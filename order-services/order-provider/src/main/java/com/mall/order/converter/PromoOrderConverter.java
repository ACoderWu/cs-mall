package com.mall.order.converter;

import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.dto.CreateSeckillOrderRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 9:43
 * @Version: 1.0
 */
@Component
public class PromoOrderConverter {
    public CreateOrderContext toCreateOrderRequest(CreateSeckillOrderRequest seckillOrderRequest) {
        CreateOrderContext orderContext = new CreateOrderContext();
        ArrayList<Long> productIds = new ArrayList<>();
        productIds.add(seckillOrderRequest.getProductId());
        orderContext.setBuyProductIds(productIds);
        orderContext.setUserId(seckillOrderRequest.getUserId());
        orderContext.setUserName(seckillOrderRequest.getUsername());
        orderContext.setOrderTotal(seckillOrderRequest.getPrice());
        return orderContext;
    }
}
