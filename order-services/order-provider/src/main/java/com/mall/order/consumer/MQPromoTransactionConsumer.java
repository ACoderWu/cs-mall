package com.mall.order.consumer;
import com.alibaba.fastjson.JSON;
import com.mall.order.PromoOrderService;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.constant.PromoRetCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;


/**
 * @Author: Li Qing
 * @Create: 2020/5/18 17:50
 * @Version: 1.0
 */

@Slf4j
@Component
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
        log.info("PromoOrderConsumer->初始化...topic:{},addr:{}", topic, addr);
        mqPushConsumer = new DefaultMQPushConsumer("promo_order_group");
        mqPushConsumer.setNamesrvAddr(addr);
        mqPushConsumer.subscribe(topic, "*");
        mqPushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            MessageExt msg = msgs.get(0);
            String str = new String(msg.getBody());
            HashMap map = JSON.parseObject(str, HashMap.class);
            CreateSeckillOrderRequest request = new CreateSeckillOrderRequest();
            request.setUsername((String) map.get("userName"));
            Integer id = (Integer) map.get("productId");
            Long productId = id.longValue();

            request.setProductId(productId);
            Integer uid = (Integer) map.get("userId");
            Long userId = uid.longValue();
            request.setUserId(userId);
            request.setPrice((BigDecimal) map.get("price"));
            CreateSeckillOrderResponse response = promoOrderService.createPromoOrder(request);
            if (!PromoRetCode.SUCCESS.getCode().equals(response.getCode())) {
                if (msg.getReconsumeTimes() > 16)
                    log.error("MQPromoTransactionConsumer", JSON.toJSONString(request), response.getMsg());
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
    }
}
