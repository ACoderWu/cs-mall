package com.mall.promo.services;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.PromoOrderService;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.PromoService;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dal.entitys.PromoSession;
import com.mall.promo.dal.persistence.PromoItemMapper;
import com.mall.promo.dal.persistence.PromoSessionMapper;
import com.mall.promo.dto.*;
import com.mall.promo.mq.MQPromoTransactionProducer;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 21:21
 * @Version: 1.0
 */
@Service
@Component
public class PromoServiceImpl implements PromoService {
    @Autowired
    PromoItemMapper promoItemMapper;
    @Autowired
    PromoSessionMapper promoSessionMapper;
    @Reference(check = false)
    PromoOrderService promoOrderService;
    @Autowired
    MQPromoTransactionProducer promoTransactionProducer;
    @Autowired
    CacheManager cacheManager;

    @Override
    public PromoInfoResponse queryPromoInfo(PromoInfoRequest request) {
        request.requestCheck();
        PromoInfoResponse response = new PromoInfoResponse();
        Integer sessionId = request.getSessionId();
        String yyyyMMdd = request.getYyyyMMdd();
        //TODO:假设每天只有一场活动
        List<Long> psIdList = selectPsId(sessionId, yyyyMMdd);
        if (psIdList.size() < 1) {
            response.setCode(PromoRetCode.PROMO_NO_EXIST.getCode());
            response.setMsg(PromoRetCode.PROMO_NO_EXIST.getMessage());
            return response;
        }
        Long psId = psIdList.get(0);
        //查询参与秒杀的productList
        try {
            List<PromoItemInfoDto> productList = promoItemMapper.selectPromoItemInfoDtoByPsId(psId);
            response.setCode(PromoRetCode.SUCCESS.getCode());
            response.setPsId(psId);
            response.setSessionId(sessionId);
            response.setProductList(productList);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BizException(PromoRetCode.DB_EXCEPTION.getMessage());
        }
        return response;
    }

    /**
     * 分布式事务创建秒杀订单
     *
     * @param request
     * @return
     */
    @Override
    public CreatePromoOrderResponse createPromoOrderInTransaction(CreatePromoOrderRequest request) {
        CreatePromoOrderResponse response = new CreatePromoOrderResponse();
        request.requestCheck();
        String key = "PROMO_ORDER_INVENTORY_NO_ENOUGH" + "-" + request.getPsId() + "-" + request.getProductId();
        String value = "true";
        ////放到controller检查
        //if (value.equals(cacheManager.checkCache(key))) {
        //    response.setCode(PromoRetCode.STOCK_NO_ENOUGH.getCode());
        //    response.setMsg(PromoRetCode.STOCK_NO_ENOUGH.getMessage());
        //    return response;
        //}
        if (promoTransactionProducer.sendPromoOrderTransaction(request)) {
            PromoItem promoItem = getPromoItem(request);
            Integer stockNum = promoItem.getStockNum();
            response.setInventory(stockNum);
            if (stockNum < 1) {
                cacheManager.setCache(key, value, 1);
            }
            response.setProductId(promoItem.getItemId());
            response.setCode(PromoRetCode.SUCCESS.getCode());
            response.setMsg(PromoRetCode.SUCCESS.getMessage());
            return response;
        }
        response.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
        response.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
        return response;
    }

    @Override
    public CreatePromoOrderResponse createPromoOrder(CreatePromoOrderRequest request) {
        CreatePromoOrderResponse response = new CreatePromoOrderResponse();
        request.requestCheck();
        try {
            if (updatePromoInventory(request) < 0) {
                response.setCode(PromoRetCode.STOCK_NO_ENOUGH.getCode());
                response.setCode(PromoRetCode.STOCK_NO_ENOUGH.getMessage());
                return response;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BizException(PromoRetCode.DB_EXCEPTION.getMessage());
        }
        PromoItem promoItem = getPromoItem(request);
        CreateSeckillOrderRequest seckillOrderRequest = new CreateSeckillOrderRequest();
        seckillOrderRequest.setUserId(request.getUserId());
        seckillOrderRequest.setUsername(request.getUserName());
        seckillOrderRequest.setProductId(request.getProductId());
        seckillOrderRequest.setPrice(promoItem.getSeckillPrice());
        CreateSeckillOrderResponse createSeckillOrderResponse = promoOrderService.createPromoOrder(seckillOrderRequest);

        if (!createSeckillOrderResponse.getCode().equals(PromoRetCode.SUCCESS.getCode())) {
            response.setCode(createSeckillOrderResponse.getCode());
            response.setMsg(createSeckillOrderResponse.getMsg());
            return response;
        }
        response.setCode(PromoRetCode.SUCCESS.getCode());
        response.setProductId(request.getProductId());
        response.setInventory(promoItem.getStockNum());
        return response;
    }

    /**
     * 获取最新的库存
     *
     * @param request
     * @return
     */
    private PromoItem getPromoItem(CreatePromoOrderRequest request) {
        Example example = new Example(PromoItem.class);
        example.createCriteria()
                .andEqualTo("itemId", request.getProductId())
                .andEqualTo("psId", request.getPsId());
        PromoItem promoItem = promoItemMapper.selectOneByExample(example);
        if (promoItem == null) throw new BizException(PromoRetCode.DB_EXCEPTION.getMessage());
        return promoItem;
    }

    /**
     * 更新秒杀库存
     *
     * @param request
     * @return
     * @throws SQLException
     */

    private Integer updatePromoInventory(CreatePromoOrderRequest request) throws SQLException {
        Long productId = request.getProductId();
        Long psId = request.getPsId();
        Integer num = 1;
        return promoItemMapper.updateStock(productId, psId, num);
    }

    /**
     * 查询秒杀活动场次
     *
     * @param sessionId
     * @param yyyyMMdd
     * @return
     */
    private List<Long> selectPsId(Integer sessionId, String yyyyMMdd) {
        Example example = new Example(PromoSession.class);
        example.createCriteria().andEqualTo("sessionId", sessionId).andEqualTo("yyyyMMdd", yyyyMMdd);
        List<PromoSession> promoSessions = promoSessionMapper.selectByExample(example);
        return promoSessions.stream().map(PromoSession::getId).collect(Collectors.toList());
    }

    public CreateSeckillOrderRequest toSecKillOrderRequest(CreatePromoOrderRequest request) {
        CreateSeckillOrderRequest seckillOrderRequest = new CreateSeckillOrderRequest();
        seckillOrderRequest.setUserId(request.getUserId());
        seckillOrderRequest.setUsername(request.getUserName());
        seckillOrderRequest.setProductId(request.getProductId());
        seckillOrderRequest.setPrice(getPromoItem(request).getSeckillPrice());
        return seckillOrderRequest;
    }
}
