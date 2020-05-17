package com.mall.order.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.converter.OrderConverter;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.OrderShipping;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ciggar
 * create-date: 2019/7/30-上午10:04
 */
@Slf4j
@Component
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    OrderShippingMapper orderShippingMapper;
    @Autowired
    OrderConverter orderConverter;
    @Autowired
    StockMapper stockMapper;

    @Override
    public OrderListResponse queryOrderList(OrderListRequest orderListRequest) {
        PageHelper.startPage(orderListRequest.getPage(), orderListRequest.getSize());
        PageHelper.orderBy(orderListRequest.getSort());
        List<OrderDetailInfo> orders = orderMapper.queryOrderDetailListByUID(orderListRequest.getUserId());
        OrderListResponse orderListResponse = new OrderListResponse();
        orderListResponse.setTotal(PageInfo.of(orders).getTotal());
        if (!CollectionUtils.isEmpty(orders)) {
            for (OrderDetailInfo order : orders) {
                String orderId = order.getOrderId();
                List<OrderItemDto> orderItemDto = orderItemMapper.queryOrderItemDtoByOrderId(orderId);
                OrderShippingDto orderShippingDto = orderShippingMapper.queryOrderShippingDtoByOrderId(orderId);
                order.setOrderItemDto(orderItemDto);
                order.setOrderShippingDto(orderShippingDto);
            }
        }
        orderListResponse.setDetailInfoList(orders);
        return orderListResponse;
    }

    @Override
    public SpecialOrderResponse queryOrder(SpecialOrderRequest request) {
        String orderId = request.getOrderId().toString();
        Order order = selectOrderByOrderId(orderId);
        //非法访问
        if (request.getUserId().equals(order.getUserId()))
            throw new BizException("非法访问");
        OrderShipping shipping = selectShippingInfoByOrderId(orderId);

        SpecialOrderResponse specialOrderResponse = orderConverter.orderAndShipping2specialRes(order, shipping);
        List<OrderItemDto> goodsList = orderItemMapper.queryOrderItemDtoByOrderId(orderId);
        specialOrderResponse.setGoodsList(goodsList);
        return specialOrderResponse;

    }

    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        String orderId = request.getOrderId();
        Long userId = request.getUserId();
        validateToken(orderId, userId);
        updateStock(orderItemMapper.queryOrderItemDtoByOrderId(orderId));
        deleteOrderByOrderId(orderId);
        deleteOrderItemByOrderId(orderId);
        deleteShippingByOrderId(orderId);
        return new CancelOrderResponse("成功");

    }

    private Boolean updateStock(List<OrderItemDto> orderItemDtoList) {
        List<Long> itemIds = orderItemDtoList.stream().map(orderItemDto -> Long.getLong(orderItemDto.getItemId())).collect(Collectors.toList());
        //锁定库存
        stockMapper.findStocksForUpdate(itemIds);
        for (OrderItemDto dto : orderItemDtoList) {
            Stock stock = new Stock();
            Long itemId = Long.getLong(dto.getItemId());
            Integer num = dto.getNum();
            stock.setLockCount(-num);
            stock.setItemId(itemId);
            stock.setStockCount(num.longValue());
            try {
                stockMapper.updateStock(stock);
            } catch (SQLException e) {
                throw new BizException(OrderRetCode.DB_EXCEPTION.getMessage());
            }
        }
        return true;


    }

    private Boolean validateToken(String orderId, Long userId) {
        Example example = new Example(Order.class);
        example.createCriteria().andEqualTo("orderId", orderId).andEqualTo("userId", userId);
        if (orderMapper.selectCountByExample(example) < 1) throw new BizException("非法访问");
        return true;
    }

    @Override
    public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {

        String orderId = request.getOrderId();
        Long userId = request.getUserId();
        validateToken(orderId, userId);
        deleteOrderByOrderId(orderId);
        deleteOrderItemByOrderId(orderId);
        deleteShippingByOrderId(orderId);
        return new DeleteOrderResponse("成功");
    }

    private void deleteShippingByOrderId(String orderId) {
        Example example = new Example(OrderShipping.class);
        example.createCriteria().andEqualTo(orderId);
        if (orderShippingMapper.deleteByExample(example) < 1)
            throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                    OrderRetCode.DB_EXCEPTION.getMessage());
    }

    private void deleteOrderItemByOrderId(String orderId) {
        Example example = new Example(OrderItem.class);
        example.createCriteria().andEqualTo(orderId);
        if (orderMapper.deleteByExample(example) < 1) throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                OrderRetCode.DB_EXCEPTION.getMessage());

    }

    private void deleteOrderByOrderId(String orderId) {
        Example example = new Example(Order.class);
        example.createCriteria().andEqualTo(orderId);
        if (orderItemMapper.deleteByExample(example) < 1) throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                OrderRetCode.DB_EXCEPTION.getMessage());
    }

    private OrderShipping selectShippingInfoByOrderId(String orderId) {
        Example example = new Example(OrderShipping.class);
        example.createCriteria().andEqualTo(orderId);
        return orderShippingMapper.selectOneByExample(example);
    }

    private Order selectOrderByOrderId(String orderId) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria().andEqualTo(orderId);
        return orderMapper.selectOneByExample(example);
    }

    @Override
    public Boolean checkPayStatus(String orderId) {
        Example example = new Example(Order.class);
        example.createCriteria().andEqualTo(orderId).andIsNotNull("paymentTime");
        return orderMapper.selectCountByExample(example) > 0;
    }
}


