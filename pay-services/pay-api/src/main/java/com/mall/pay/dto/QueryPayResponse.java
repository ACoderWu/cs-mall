package com.mall.pay.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Li Qing
 * @Create: 2020/5/20 17:21
 * @Version: 1.0
 * @Description: 获取付款二维码请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class QueryPayResponse extends AbstractResponse {
    private static final long serialVersionUID = 7124011968677187232L;
}
