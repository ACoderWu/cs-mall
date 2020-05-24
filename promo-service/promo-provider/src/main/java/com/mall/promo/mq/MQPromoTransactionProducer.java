package com.mall.promo.mq;

import com.alibaba.fastjson.JSON;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.converter.PromoOrderRequestConverter;
import com.mall.promo.dal.entitys.PromoItem;
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
import tk.mybatis.mapper.entity.Example;

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
        transactionMQProducer = new TransactionMQProducer("promo_order_group");
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
                Long productId = (Long) argMap.get("productId");
                Long psId = (Long) argMap.get("psId");
                String key = "promo_order" + message.getTransactionId();
                if (promoItemMapper.updateStock(productId, psId, 1) < 1) {
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
        Map<String, Object> map = new HashMap<>();
        HashMap<String, Object> argMap = new HashMap<>();
        //CreateSeckillOrderRequest seckillOrderRequest = converter.toSecKillRequest(request);
        Long productId = request.getProductId();
        Long psId = request.getPsId();
        map.put("productId", productId);
        argMap.put("productId", productId);
        argMap.put("psId", psId);
        map.put("price", getPromoItem(psId, productId).getSeckillPrice());
        map.put("userId", request.getUserId());
        map.put("userName", request.getUserName());
        message.setBody(JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8));

        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argMap);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return sendResult != null && LocalTransactionState.COMMIT_MESSAGE.equals(sendResult.getLocalTransactionState());
    }

    /**
     * 获取相关商品库存信息
     *
     * @param psId
     * @param productId
     * @return
     */
    private PromoItem getPromoItem(Long psId, Long productId) {
        Example example = new Example(PromoItem.class);
        example.createCriteria().andEqualTo("psId", psId).andEqualTo("itemId", productId);
        return promoItemMapper.selectByExample(example).get(0);
    }
}
