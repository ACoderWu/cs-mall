package com.mall.promo.mq;

import com.alibaba.fastjson.JSON;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.converter.PromoOrderRequestConverter;
import com.mall.promo.dal.persistence.PromoItemMapper;
import com.mall.promo.dto.CreatePromoOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 11:37
 * @Version: 1.0
 */
@Component
@Slf4j
public class MQPromoTransactionProducer {
    private TransactionMQProducer transactionMQProducer;
    @Value("${mq.nameserver.addr}")
    String addr;
    @Value("${mq.topicname}")
    String topic;
    @Autowired
    PromoItemMapper promoItemMapper;
    @Autowired
    CacheManager cacheManager;
    @Autowired
    PromoOrderRequestConverter converter;

    @PostConstruct
    public void init() {
        transactionMQProducer = new TransactionMQProducer("promo_group");
        transactionMQProducer.setNamesrvAddr(addr);
        try {
            transactionMQProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        //注册一个事务监听器
        transactionMQProducer.setTransactionListener(new TransactionListener() {

            /**
             * 执行本地事务
             * @param message
             * @param o
             * @return
             */
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                //FIXME:此处还需要统一CreatePromoOrderRequest和CreateSeckillOrderRequest
                HashMap argMap = (HashMap<String, Object>) o;
                CreatePromoOrderRequest request = (CreatePromoOrderRequest) argMap.get("arg");
                String key = "promo_order" + message.getTransactionId();
                if (promoItemMapper.updateStock(request.getProductId(), request.getPsId(), 1) < 1) {
                    cacheManager.setCache(key, PromoRetCode.SYSTEM_ERROR.getCode(), 1);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                cacheManager.setCache(key, PromoRetCode.SUCCESS.getCode(), 1);
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            /**
             * 检查本地事务
             * @param messageExt
             * @return
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String key = "promo_order" + messageExt.getTransactionId();
                String value = cacheManager.checkCache(key);
                if (PromoRetCode.SUCCESS.getCode().equals(value))
                    return LocalTransactionState.COMMIT_MESSAGE;
                else if (PromoRetCode.SYSTEM_ERROR.getCode().equals(value))
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                else return LocalTransactionState.UNKNOW;
            }
        });

    }

    public Boolean sendPromoOrderTransaction(CreatePromoOrderRequest request) {
        Message message = new Message();
        message.setTopic(topic);
        CreateSeckillOrderRequest seckillOrderRequest = converter.toSecKillRequest(request);
        message.setBody(JSON.toJSONString(seckillOrderRequest).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> argMap = new HashMap<>();
        argMap.put("arg", request);
        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argMap);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return sendResult != null && LocalTransactionState.COMMIT_MESSAGE.equals(sendResult.getLocalTransactionState());
    }
}
