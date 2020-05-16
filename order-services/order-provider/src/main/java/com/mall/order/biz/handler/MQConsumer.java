package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.order.OrderQueryService;
import com.mall.order.dto.CancelOrderRequest;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Li Qing
 * @Create: 2020/5/15 16:40
 * @Version: 1.0
 */
@Component
public class MQConsumer {
    private DefaultMQPushConsumer mqConsumer;
    @Autowired
    private OrderQueryService orderQueryService;

    @PostConstruct
    private void init() throws MQClientException {
        mqConsumer = new DefaultMQPushConsumer("consumer_group");
        mqConsumer.setNamesrvAddr("localhost:9876");
        mqConsumer.subscribe("topic_order", "*");
        //设置消息监听器
        mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext
                    consumeConcurrentlyContext) {
                MessageExt messageExt = list.get(0);
                byte[] body = messageExt.getBody();
                System.out.println(Arrays.toString(body));
                String msg = new String(body);
                HashMap<String, Object> map = JSON.parseObject(msg, HashMap.class);
                CancelOrderRequest request = (CancelOrderRequest) map.get("cancelOrderRequest");
                //检查支付状态，未支付则取消订单
                if (!orderQueryService.checkPayStatus(request.getOrderId()))
                    orderQueryService.cancelOrder(request);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        mqConsumer.start();
        System.out.println(System.currentTimeMillis());
    }


}
