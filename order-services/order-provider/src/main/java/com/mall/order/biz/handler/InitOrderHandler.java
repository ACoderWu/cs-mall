package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dto.CartProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ciggar
 * create-date: 2019/8/1-下午5:01
 * 初始化订单 生成订单
 */

@Slf4j
@Component
public class InitOrderHandler extends AbstractTransHandler {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;

    @Override

    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;

        Order order = new Order();
        //插入order
        //FIXME:后续使用发号器
        String orderId = UUID.randomUUID().toString();
        order.setOrderId(orderId);
        order.setUserId(createOrderContext.getUserId());
        order.setBuyerNick(createOrderContext.getUserName());
        order.setPayment(createOrderContext.getOrderTotal());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setStatus(OrderConstants.ORDER_STATUS_INIT);
        orderMapper.insert(order);

        List<Long> productIds = new ArrayList<>();
        //插入order关联表
        List<CartProductDto> dtoList = createOrderContext.getCartProductDtoList();
        if (dtoList != null) {
            for (CartProductDto dto : dtoList) {

                OrderItem orderItem = new OrderItem();
                String orderItemId = UUID.randomUUID().toString();
                orderItem.setId(orderItemId);
                Long productId = dto.getProductId();
                //收集productId更新createOrderContext
                productIds.add(productId);
                orderItem.setItemId(productId);
                orderItem.setOrderId(orderId);
                //商品限购处理
                Long num = dto.getProductNum();
                Long limitNum = dto.getLimitNum();
                if (limitNum != null) {
                    num = limitNum < num ? limitNum : num;
                }
                orderItem.setNum(num.intValue());
                double price = dto.getSalePrice().doubleValue();
                orderItem.setPrice(price);
                orderItem.setPicPath(dto.getProductImg());
                orderItem.setTotalFee(price * num);
                orderItem.setTitle(dto.getProductName());
                orderItem.setStatus(1);
                int code = orderItemMapper.insert(orderItem);
                if (code < 1) throw new BizException(OrderRetCode.DB_EXCEPTION.getCode());
            }
        }
        createOrderContext.setOrderId(orderId);
        //FIXME:也许可以在更新库存时更新次操作
        if (CollectionUtils.isEmpty(createOrderContext.getBuyProductIds()))
            createOrderContext.setBuyProductIds(productIds);
        return true;
    }
}
