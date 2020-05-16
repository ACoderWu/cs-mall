package com.mall.order.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.order.dal.entitys.OrderShipping;
import com.mall.order.dto.OrderShippingDto;

public interface OrderShippingMapper extends TkMapper<OrderShipping> {
    OrderShippingDto queryOrderShippingDtoByOrderId(String orderId);
}