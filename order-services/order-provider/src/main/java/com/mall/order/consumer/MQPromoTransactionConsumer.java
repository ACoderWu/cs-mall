package com.mall.order.consumer;

import com.alibaba.fastjson.JSON;
import com.mall.order.PromoOrderService;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.constant.PromoRetCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 17:50
 * @Version: 1.0
 */
@Component
@Slf4j
public class MQPromoTransactionConsumer {
    private DefaultMQPushConsumer mqPushConsumer;
    @Value("${mq.nameserver.addr}")
    String addr;
    @Value("${mq.topicname}")
    String topic;
    @Autowired
    PromoOrderService promoOrderService;

    @PostConstruct
    public void init() throws MQClientException {
        mqPushConsumer = new DefaultMQPushConsumer();
        mqPushConsumer.setConsumerGroup("order");
        mqPushConsumer.setNamesrvAddr(addr);
        mqPushConsumer.subscribe(topic, "*");
        mqPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                String str = new String(msg.getBody());
                CreateSeckillOrderRequest request = JSON.parseObject(str, CreateSeckillOrderRequest.class);
                CreateSeckillOrderResponse response = promoOrderService.createPromoOrder(request);
                if (!PromoRetCode.SUCCESS.getCode().equals(response.getCode())) {
                    if (msg.getReconsumeTimes() > 16)
                        log.error("MQPromoTransactionConsumer", JSON.toJSONString(request), response.getMsg());
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        mqPushConsumer.start();
    }
}
