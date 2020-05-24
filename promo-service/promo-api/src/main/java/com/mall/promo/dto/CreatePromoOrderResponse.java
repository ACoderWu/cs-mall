package com.mall.promo.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Li Qing
 * @Create: 2020/5/17 11:20
 * @Version: 1.0
 * 秒杀下单response
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreatePromoOrderResponse extends AbstractResponse {
    private static final long serialVersionUID = -8200319800761089206L;
    private Long productId;
    private Integer inventory;
}
