package com.mall.order.converter;

import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.dto.CreateSeckillOrderRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 17:47
 * @Version: 1.0
 */
@Component
public class PromoOrderConverter {
    /**
     * 转换CreateSeckillOrderRequest 为 CreateOrderContext
     *
     * @param request
     * @return
     */
    public CreateOrderContext toCreateOrderContext(CreateSeckillOrderRequest request) {
        CreateOrderContext orderContext = new CreateOrderContext();
        ArrayList<Long> productIds = new ArrayList<>();
        productIds.add(request.getProductId());
        orderContext.setBuyProductIds(productIds);
        orderContext.setUserId(request.getUserId());
        orderContext.setUserName(request.getUsername());
        orderContext.setBuyerNickName(request.getUsername());
        orderContext.setOrderTotal(request.getPrice());
        return orderContext;
    }


}
