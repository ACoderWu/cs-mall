package com.mall.order.services;

import com.mall.order.PromoOrderService;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.handler.InitOrderHandler;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.dto.CreatePromoOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 21:20
 * @Version: 1.0
 */
@Slf4j
@Component
@Service(cluster = "failfast")
public class PromoOrderServiceImpl implements PromoOrderService {

    @Autowired
    InitOrderHandler handler;

    @Override
    public CreateSeckillOrderResponse createPromoOrder(CreateSeckillOrderRequest request) {
        //FIXME:此处还差一个转换
        CreateSeckillOrderResponse response = new CreateSeckillOrderResponse();
        CreateOrderContext orderContext = new CreateOrderContext();
        if (handler.handle(orderContext)) {
            response.setCode(OrderRetCode.SUCCESS.getCode());
            return response;
        }
        response.setCode(OrderRetCode.INIT_ORDER_EXCEPTION.getCode());
        response.setMsg(OrderRetCode.INIT_ORDER_EXCEPTION.getMessage());
        return response;
    }
}
