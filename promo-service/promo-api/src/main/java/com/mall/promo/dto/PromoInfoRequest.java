package com.mall.promo.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.promo.constant.PromoRetCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 17:30
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
public class PromoInfoRequest extends AbstractRequest {
    private static final long serialVersionUID = -3907282009666799224L;
    private Integer sessionId;
    private String yyyyMMdd;

    @Override
    public void requestCheck() {
        if (sessionId == null || StringUtils.isBlank(yyyyMMdd))
            throw new ValidateException(PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(), PromoRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
    }
}
