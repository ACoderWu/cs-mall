package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.order.dto.CancelOrderRequest;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Li Qing
 * @Create: 2020/5/15 16:35
 * @Version: 1.0
 */
@Component
public class MQProducer {
    private DefaultMQProducer producer;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("consumer_group");
        producer.setNamesrvAddr("127.0.0.1:9876");
        producer.start();
    }

    public void sendOrderMessage(CancelOrderRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", request.getOrderId());
        map.put("userId",request.getUserId());
        Message message = new Message("order_topic", JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8));
        //延迟半小时发送
        //message.setDelayTimeLevel(16);
        //测试5s取消订单
        message.setDelayTimeLevel(16);
        try {
            producer.send(message);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
