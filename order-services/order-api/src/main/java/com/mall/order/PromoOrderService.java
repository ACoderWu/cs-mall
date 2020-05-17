package com.mall.order;

import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.dto.CreatePromoOrderRequest;
import com.mall.promo.dto.CreatePromoOrderResponse;

/**
 *  ciggar
 * create-date: 2019/7/30-上午9:13
 * 订单相关业务
 */
public interface PromoOrderService {


    CreateSeckillOrderResponse createPromoOrder(CreateSeckillOrderRequest request);
}
