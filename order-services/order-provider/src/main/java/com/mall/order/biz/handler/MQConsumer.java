package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.order.OrderQueryService;
import com.mall.order.dto.CancelOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Li Qing
 * @Create: 2020/5/15 16:40
 * @Version: 1.0
 */
@Component
@Slf4j
public class MQConsumer {
    private DefaultMQPushConsumer mqConsumer;
    @Autowired
    private OrderQueryService orderQueryService;

    @PostConstruct
    private void init() throws MQClientException {
        log.info("MQConsume->初始化");
        mqConsumer = new DefaultMQPushConsumer("consumer_group");
        mqConsumer.setNamesrvAddr("127.0.0.1:9876");
        mqConsumer.subscribe("order_topic", "*");
        //设置消息监听器
        mqConsumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
            MessageExt messageExt = list.get(0);
            byte[] body = messageExt.getBody();
            String msg = new String(body);
            HashMap<String, Object> map = (HashMap<String, Object>) JSON.parseObject(msg, Map.class);
            String orderId = (String) map.get("orderId");
            Integer userId = (Integer) map.get("userId");
            CancelOrderRequest request = CancelOrderRequest.builder().orderId(orderId).userId(userId.longValue()).build();
            //检查支付状态，未支付则取消订单
            if (!orderQueryService.checkPayStatus(request.getOrderId()))
                orderQueryService.cancelOrder(request);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqConsumer.start();
    }


}
