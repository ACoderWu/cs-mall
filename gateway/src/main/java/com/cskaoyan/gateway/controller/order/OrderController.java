package com.cskaoyan.gateway.controller.order;

import com.mall.order.OrderCoreService;
import com.mall.order.OrderQueryService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 21:53
 * @Version: 1.0
 */
@RestController
@RequestMapping("/shopping")
public class OrderController {
    @Reference(check = false)
    OrderQueryService orderQueryService;
    @Reference(check = false)
    OrderCoreService orderCoreService;
}
