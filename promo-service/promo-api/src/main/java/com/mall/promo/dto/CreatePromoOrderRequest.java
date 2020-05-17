package com.mall.promo.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.promo.constant.PromoRetCode;

/**
 * @Author: Li Qing
 * @Create: 2020/5/17 11:20
 * @Version: 1.0
 * 秒杀下单request
 */
public class CreatePromoOrderRequest extends AbstractRequest {
    private static final long serialVersionUID = 4747051285946558090L;
    private Long psId;
    private Long productId;

    @Override
    public void requestCheck() {
        if (psId == null || productId == null)
            throw new ValidateException(PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
    }
}
