package com.mall.pay.dto;

import com.mall.pay.constant.PayRetCode;
import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: Li Qing
 * @Create: 2020/5/20 17:21
 * @Version: 1.0
 * @Description: 获取付款二维码请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryPayRequest extends AbstractRequest {

    private static final long serialVersionUID = -5944778455433486796L;
    private String orderId;
    private Long uid;

    @Override
    public void requestCheck() {
        if (StringUtils.isBlank(orderId) || uid == null)
            throw new ValidateException(PayRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), PayRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());

    }
}
