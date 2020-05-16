package com.cskaoyan.gateway.controller.promo;


import com.mall.commons.result.ResponseData;
import com.mall.promo.PromoService;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

        return new ResponseData();
    }


}
