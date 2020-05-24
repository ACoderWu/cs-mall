package com.mall.order.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
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
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.sql.SQLException;
import java.util.Date;
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
    //@Autowired
    //OrderConverter orderConverter;
    @Autowired
    StockMapper stockMapper;

    @Override
    public OrderListResponse queryOrderList(OrderListRequest orderListRequest) {
        PageHelper.startPage(orderListRequest.getPage(), orderListRequest.getSize());
        String sort = orderListRequest.getSort();
        if (!StringUtils.isBlank(sort)) {
            PageHelper.orderBy(sort);
        }
        List<OrderDetailInfo> orders = null;
        OrderListResponse orderListResponse = new OrderListResponse();
        orders = orderMapper.queryOrderDetailListByUID(orderListRequest.getUserId());
        if (orders == null) {
            orderListResponse.setCode(OrderRetCode.DB_EXCEPTION.getCode());
            orderListResponse.setMsg(OrderRetCode.DB_EXCEPTION.getMessage());
            return orderListResponse;
        }
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
        orderListResponse.setCode(OrderRetCode.SUCCESS.getCode());
        orderListResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
        return orderListResponse;
    }

    @Override
    public SpecialOrderResponse queryOrder(SpecialOrderRequest request) {
        request.requestCheck();
        SpecialOrderResponse specialOrderResponse = new SpecialOrderResponse();
        String orderId = request.getOrderId();
        Order order = selectOrderByOrderId(orderId);
        if (order != null) {
            //非法访问
            if (!request.getUserId().equals(order.getUserId()))
                throw new BizException("非法访问");
            specialOrderResponse.setOrderTotal(order.getPayment().doubleValue());
            specialOrderResponse.setUserId(order.getUserId());
            specialOrderResponse.setUserName(order.getBuyerNick());
        }
        OrderShipping shipping = selectShippingInfoByOrderId(orderId);
        //SpecialOrderResponse specialOrderResponse = orderConverter.orderAndShipping2specialRes(order, shipping);
        if (shipping != null) {
            specialOrderResponse.setTel(shipping.getReceiverPhone());
            specialOrderResponse.setStreetName(shipping.getReceiverAddress());
        }
        List<OrderItemDto> goodsList = orderItemMapper.queryOrderItemDtoByOrderId(orderId);
        specialOrderResponse.setGoodsList(goodsList);
        specialOrderResponse.setCode(OrderRetCode.SUCCESS.getCode());
        specialOrderResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
        return specialOrderResponse;

    }

    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        request.requestCheck();
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        String orderId = request.getOrderId();
        Long userId = request.getUserId();
        try {
            validateToken(orderId, userId);
            updateStock(orderItemMapper.queryOrderItemDtoByOrderId(orderId));
            deleteOrderByOrderId(orderId);
            deleteOrderItemByOrderId(orderId);
            deleteShippingByOrderId(orderId);
        } catch (BizException e) {
            cancelOrderResponse.setCode(e.getErrorCode());
            cancelOrderResponse.setMsg(e.getMessage());
            return cancelOrderResponse;
        }
        cancelOrderResponse.setCode(OrderRetCode.SUCCESS.getCode());
        cancelOrderResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
        cancelOrderResponse.setResult(OrderRetCode.SUCCESS.getMessage());
        return cancelOrderResponse;

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
        if (orderMapper.selectCountByExample(example) < 1)
            throw new BizException(OrderRetCode.SYSTEM_ERROR.getCode(), "非法访问");
        return true;
    }

    @Override
    @Transactional
    public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {
        DeleteOrderResponse response = new DeleteOrderResponse();
        String orderId = request.getOrderId();
        Long userId = request.getUserId();
        try {
            validateToken(orderId, userId);
            deleteOrderByOrderId(orderId);
            deleteOrderItemByOrderId(orderId);
            deleteShippingByOrderId(orderId);
        } catch (BizException e) {
            response.setCode(e.getErrorCode());
            response.setMsg(e.getMessage());
            return response;
        }
        response.setCode(OrderRetCode.SUCCESS.getCode());
        response.setMsg(OrderRetCode.SUCCESS.getMessage());
        response.setResult(OrderRetCode.SUCCESS.getMessage());
        return response;
    }

    private void deleteShippingByOrderId(String orderId) {
        //FIXME：暂时取消对物流信息删除而抛异常
        Example example = new Example(OrderShipping.class);
        example.createCriteria().andEqualTo("orderId", orderId);
        orderShippingMapper.deleteByExample(example);
    }

    private void deleteOrderItemByOrderId(String orderId) {
        Example example = new Example(OrderItem.class);
        example.createCriteria().andEqualTo("orderId", orderId);
        if (orderMapper.deleteByExample(example) < 1) throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                OrderRetCode.DB_EXCEPTION.getMessage());

    }

    private void deleteOrderByOrderId(String orderId) {
        Example example = new Example(Order.class);
        example.createCriteria().andEqualTo("orderId", orderId);
        if (orderItemMapper.deleteByExample(example) < 1) throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                OrderRetCode.DB_EXCEPTION.getMessage());
    }

    private OrderShipping selectShippingInfoByOrderId(String orderId) {
        Example example = new Example(OrderShipping.class);
        example.createCriteria().andEqualTo("orderId", orderId);
        return orderShippingMapper.selectOneByExample(example);
    }

    private Order selectOrderByOrderId(String orderId) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria().andEqualTo("orderId", orderId);
        List<Order> orders = orderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(orders))
            throw new BizException(OrderRetCode.DB_EXCEPTION.getMessage(), OrderRetCode.DB_EXCEPTION.getCode());
        return orders.get(0);
    }

    @Override
    public Boolean checkPayStatus(String orderId) {
        Example example = new Example(Order.class);
        example.createCriteria().andEqualTo("orderId", orderId).andIsNotNull("paymentTime");
        return orderMapper.selectCountByExample(example) > 0;
    }

    /**
     * 更新订单支付状态
     *
     * @param orderId
     * @return
     */
    @Override
    public int updatePayStatus(String orderId) {
        Date date = new Date();
        Order order = Order.builder().orderId(orderId).paymentTime(date).status(1).updateTime(date).build();
        return orderMapper.updateByPrimaryKeySelective(order);
    }
}



