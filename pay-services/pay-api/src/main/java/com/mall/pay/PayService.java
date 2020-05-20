package com.mall.pay;

import com.mall.pay.dto.PrePayRequest;
import com.mall.pay.dto.PrePayResponse;
import com.mall.pay.dto.QueryPayRequest;
import com.mall.pay.dto.QueryPayResponse;

/**
 * @Author: Li Qing
 * @Create: 2020/5/20 22:16
 * @Version: 1.0
 */
public interface PayService {

    PrePayResponse createPrePay(PrePayRequest request);

    QueryPayResponse queryPayStatus(QueryPayRequest request);
}
