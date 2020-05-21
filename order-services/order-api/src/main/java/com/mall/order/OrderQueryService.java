package com.mall.order;

import com.mall.order.dto.*;

/**
 * ciggar
 * create-date: 2019/7/30-上午10:01
 */
public interface OrderQueryService {


    OrderListResponse queryOrderList(OrderListRequest orderListRequest);

    SpecialOrderResponse queryOrder(SpecialOrderRequest request);

    CancelOrderResponse cancelOrder(CancelOrderRequest request);

    DeleteOrderResponse deleteOrder(DeleteOrderRequest request);

    Boolean checkPayStatus(String orderId);

    int updatePayStatus(OrderDto order);
}
