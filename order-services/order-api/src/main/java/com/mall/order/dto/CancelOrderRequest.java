package com.mall.order.dto;
/**
 * Created by ciggar on 2019/7/30.
 */

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.order.constant.OrderRetCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * ciggar
 * create-date: 2019/7/30-上午9:55
 */
@Data
@AllArgsConstructor
public class CancelOrderRequest extends AbstractRequest {

    private String orderId;
    private Long userId;

    @Override
    public void requestCheck() {
        if (StringUtils.isBlank(orderId) || userId == null) {
            throw new ValidateException(OrderRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), OrderRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
    }
}
