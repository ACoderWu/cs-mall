package com.mall.promo.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.promo.constant.PromoRetCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: Li Qing
 * @Create: 2020/5/17 11:20
 * @Version: 1.0
 * 秒杀下单request
 */
@Data
@AllArgsConstructor
public class CreatePromoOrderRequest extends AbstractRequest {
    private static final long serialVersionUID = 4747051285946558090L;
    private Long psId;
    private Long productId;
    private Long userId;
    private String userName;

    @Override
    public void requestCheck() {
        if (psId == null || productId == null || userId == null || StringUtils.isBlank(userName))
            throw new ValidateException(PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
    }
}
