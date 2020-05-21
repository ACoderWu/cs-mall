package com.mall.pay.model.result;

import com.alipay.api.response.AlipayTradeRefundResponse;
import com.mall.pay.model.TradeStatus;

/**
 * Created by liuyangkly on 15/8/27.
 */
public class AlipayF2FRefundResult implements Result {
    private TradeStatus tradeStatus;
    private AlipayTradeRefundResponse response;

    public AlipayF2FRefundResult(AlipayTradeRefundResponse response) {
        this.response = response;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public void setResponse(AlipayTradeRefundResponse response) {
        this.response = response;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public AlipayTradeRefundResponse getResponse() {
        return response;
    }

    public boolean isTradeSuccess() {
        return response != null &&
                TradeStatus.SUCCESS.equals(tradeStatus);
    }
}
