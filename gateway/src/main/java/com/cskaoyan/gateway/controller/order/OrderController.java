package com.cskaoyan.gateway.controller.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;

import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


/**
 * @Author: Li Qing
 * @Create: 2020/5/13 11:55
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/shopping")
@Api(tags = "OrderController", description = "订单层")
public class OrderController {
    @Reference(timeout = 3000, check = false)
    OrderCoreService orderCoreService;
    @Reference(timeout = 3000, check = false)
    OrderQueryService orderQueryService;

    @PostMapping("/order")
    @ApiOperation("创建订单")
    public ResponseData createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        CreateOrderResponse createOrderResponse = orderCoreService.createOrder(createOrderRequest);
        if (!createOrderResponse.getCode().equals(OrderRetCode.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(createOrderResponse.getMsg());
        return new ResponseUtil<>().setData(createOrderResponse.getOrderId());
    }

    @GetMapping("/order")
    @ApiOperation("获取当前用户的所有订单")
    public ResponseData getAllOrders(@RequestBody OrderListRequest orderListRequest, HttpServletRequest servletRequest) {
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        orderListRequest.setUserId(uid);
        OrderListResponse orderListResponse = orderQueryService.queryOrderList(orderListRequest);
        if (!orderListResponse.getCode().equals(OrderRetCode.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(orderListResponse.getMsg());
        HashMap<String, Object> data = new HashMap<>();
        data.put("data", orderListResponse);
        return new ResponseUtil<>().setData(data);
    }

    @GetMapping("/order/{id}")
    @ApiOperation("获取当前用户的特定编号订单信息")
    public ResponseData getOrder(@PathVariable("id") Long orderId, HttpServletRequest servletRequest) {
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        SpecialOrderRequest request = new SpecialOrderRequest(orderId, uid);
        SpecialOrderResponse response = orderQueryService.queryOrder(request);
        if (!response.getCode().equals(OrderRetCode.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response);
    }

    @PutMapping("/order/{id}")
    @ApiOperation("取消订单")
    public ResponseData cancleOrder(@PathVariable("id") Long orderId, HttpServletRequest servletRequest) {
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        CancelOrderRequest request = new CancelOrderRequest(orderId.toString(), uid);
        CancelOrderResponse response = orderQueryService.cancelOrder(request);
        if (!response.getCode().equals(OrderRetCode.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response.getResult());
    }

    @DeleteMapping("/order/{id}")
    @ApiOperation("删除订单")
    public ResponseData deleteOrder(@PathVariable("id") Long orderId, HttpServletRequest servletRequest) {
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        DeleteOrderRequest request = new DeleteOrderRequest(orderId.toString(),uid);
        DeleteOrderResponse response = orderQueryService.deleteOrder(request);
        if (!response.getCode().equals(OrderRetCode.SUCCESS.getCode()))
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        return new ResponseUtil<>().setData(response.getResult());
    }
}
