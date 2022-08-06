package com.imooc.food.orderservicemanager.moodymq.listener;

import com.imooc.food.orderservicemanager.moodymq.po.TransMessagePO;
import com.imooc.food.orderservicemanager.moodymq.service.TransMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Slf4j
public abstract class AbstractMessageListener implements ChannelAwareMessageListener {
    
    @Autowired
    TransMessageService transMessageService;
    @Value("${moodymq.resendTimes}")
    Integer resendTimes;
    
    public abstract void receviceMessage(Message message);
    
    @Override
    public void onMessage(Message message, Channel channel) throws IOException, InterruptedException {
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        
        TransMessagePO transMessagePO = transMessageService.messageReceiveReady(
                messageProperties.getMessageId(),
                messageProperties.getReceivedExchange(),
                messageProperties.getReceivedRoutingKey(),
                messageProperties.getConsumerQueue(),
                new String(message.getBody())
        );
        log.info("收到消息{}, 消费次数{}",
                messageProperties.getMessageId(), transMessagePO.getSequence());
        
        try {
            receviceMessage(message);
            channel.basicAck(deliveryTag, false);
            transMessageService.messageReceiveSuccess(messageProperties.getMessageId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (transMessagePO.getSequence() >= resendTimes) {
                channel.basicReject(deliveryTag, false);
            } else {
                Thread.sleep((long) (Math.pow(2, transMessagePO.getSequence())) * 1000);
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }
}
