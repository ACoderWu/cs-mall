package com.mall.promo.converter;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dal.persistence.PromoItemMapper;
import com.mall.promo.dto.CreatePromoOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 22:05
 * @Version: 1.0
 */
@Component
public class PromoOrderRequestConverter {
    @Autowired
    PromoItemMapper promoItemMapper;

    public CreateSeckillOrderRequest toSecKillRequest(CreatePromoOrderRequest request) {
        CreateSeckillOrderRequest seckillOrderRequest = new CreateSeckillOrderRequest();
        seckillOrderRequest.setUserId(request.getUserId());
        seckillOrderRequest.setUsername(request.getUserName());
        seckillOrderRequest.setProductId(request.getProductId());
        seckillOrderRequest.setPrice(getPromoItem(request).getSeckillPrice());
        return seckillOrderRequest;
    }

    private PromoItem getPromoItem(CreatePromoOrderRequest request) {
        Example example = new Example(PromoItem.class);
        example.createCriteria()
                .andEqualTo("itemId", request.getProductId())
                .andEqualTo("psId", request.getPsId());
        PromoItem promoItem = promoItemMapper.selectOneByExample(example);
        if (promoItem == null) throw new BizException(PromoRetCode.DB_EXCEPTION.getMessage());
        return promoItem;
    }

}
