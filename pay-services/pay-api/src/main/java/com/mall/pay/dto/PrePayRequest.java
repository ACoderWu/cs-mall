package com.mall.pay.dto;

import com.mall.pay.constant.PayRetCode;
import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

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
public class PrePayRequest extends AbstractRequest {
    private static final long serialVersionUID = 6279828851801626284L;
    private String nickName;
    private BigDecimal money;
    private String info;
    private String orderId;
    private String payType;
    private Long uid;

    @Override
    public void requestCheck() {
        if (StringUtils.isBlank(nickName) || money == null || StringUtils.isBlank(orderId) || StringUtils.isBlank(info) || StringUtils.isBlank(payType) || uid == null)
            throw new ValidateException(PayRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), PayRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());

    }
}
