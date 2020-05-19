package com.cskaoyan.gateway.controller.promo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cskaoyan.gateway.cache.CacheManager;
import com.google.common.util.concurrent.RateLimiter;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.promo.PromoService;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dto.*;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 21:50
 * @Version: 1.0
 */
@RequestMapping("/shopping")
@RestController
public class PromoController {
    @Reference(check = false)
    PromoService promoService;
    @Autowired
    CacheManager cacheManager;

    private RateLimiter rateLimiter;
    private ExecutorService executorService;

    @PostConstruct
    //每秒产生100个令牌
    public void init() {

        rateLimiter = RateLimiter.create(100);
        //建造容量为100的线程池
        executorService = Executors.newFixedThreadPool(100);

    }

    @GetMapping("/seckilllist")
    @Anoymous
    public ResponseData getPromoList(@RequestParam Integer sessionId) {
        String yyyyMMdd = toYYYYMMdd(new Date());
        PromoInfoRequest request = new PromoInfoRequest(sessionId, yyyyMMdd);
        PromoInfoResponse response = promoService.queryPromoInfo(request);
        if (!response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response);
    }

    @PostMapping("seckill")
    public ResponseData secKill(HttpServletRequest servletRequest, @RequestBody CreatePromoOrderInfo info) {

        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        String userName = (String) object.get("userName");
        Long productId = info.getProductId();
        Long psId = info.getPsId();
        if (!checkInventory(psId, productId))
            return new ResponseUtil<>().setErrorMsg(PromoRetCode.STOCK_NO_ENOUGH.getMessage());
        //令牌限流
        rateLimiter.acquire();
        CreatePromoOrderRequest request = new CreatePromoOrderRequest(psId, productId, uid, userName);
        //线程池泄洪
        Future<CreatePromoOrderResponse> responseFuture = executorService.submit(new Callable<CreatePromoOrderResponse>() {
            @Override
            public CreatePromoOrderResponse call() throws Exception {
                return promoService.createPromoOrderInTransaction(request);
            }
        });
        CreatePromoOrderResponse response = null;
        try {
            response = responseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (!response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response);
    }

    /**
     * 检查秒杀活动库存
     *
     * @param psId
     * @param productId
     * @return
     */
    private boolean checkInventory(Long psId, Long productId) {
        String key = "PROMO_ORDER_INVENTORY_NO_ENOUGH" + "-" + psId + "-" + productId;
        String value = "true";
        //检查库存
        return value.equals(cacheManager.checkCache(key));
    }

    /**
     * 转换时间未yyyyMMdd的字符串格式
     *
     * @param date
     * @return
     */
    private String toYYYYMMdd(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(date).toString();
    }


}
