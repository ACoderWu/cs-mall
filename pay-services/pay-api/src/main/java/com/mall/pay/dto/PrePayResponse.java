package com.mall.pay.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.*;

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
public class PrePayResponse extends AbstractResponse {
    private static final long serialVersionUID = -8468225739747778565L;
    private String QRCodeUrl;
}
