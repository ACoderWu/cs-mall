package com.mall.promo.dto;

import lombok.Data;

/**
 * @Author: Li Qing
 * @Create: 2020/5/17 11:20
 * @Version: 1.0
 * 秒杀下单request参数
 */
@Data
public class CreatePromoOrderInfo {

    private Long psId;
    private Long productId;
}
