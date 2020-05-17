package com.cskaoyan.gateway.controller.promo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.promo.PromoService;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dto.*;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        CreatePromoOrderRequest request = new CreatePromoOrderRequest(info.getPsId(), info.getProductId(), uid, userName);
        CreatePromoOrderResponse response = promoService.createPromoOrder(request);
        if (!response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response);
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
