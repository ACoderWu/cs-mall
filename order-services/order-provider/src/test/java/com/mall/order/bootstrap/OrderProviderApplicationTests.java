package com.mall.order.bootstrap;

import com.mall.order.OrderQueryService;
import com.mall.order.dto.SpecialOrderRequest;
import com.mall.order.dto.SpecialOrderResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderProviderApplicationTests {
    @Autowired
    OrderQueryService orderQueryService;

    @Test
    public void contextLoads() {
        SpecialOrderResponse specialOrderResponse = orderQueryService.queryOrder(new SpecialOrderRequest(71L, 20050402540122568L));
        System.out.println(specialOrderResponse);
    }

}
