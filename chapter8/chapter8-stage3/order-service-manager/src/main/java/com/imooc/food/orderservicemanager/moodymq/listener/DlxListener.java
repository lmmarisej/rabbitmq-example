package com.imooc.food.orderservicemanager.moodymq.listener;

import com.imooc.food.orderservicemanager.moodymq.service.TransMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("moodymq.dlxEnabled")
public class DlxListener implements ChannelAwareMessageListener {
    
    @Autowired
    TransMessageService transMessageService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String messageBody = new String(message.getBody());
        log.error("dead letter! message:{}", message);
        // 发邮件、打电话、发短信
        // XXXXX（）
        MessageProperties messageProperties = message.getMessageProperties();
        transMessageService.messageDead(
                messageProperties.getMessageId(),
                messageProperties.getReceivedExchange(),
                messageProperties.getReceivedRoutingKey(),
                messageProperties.getConsumerQueue(),
                messageBody
        );

        channel.basicAck(messageProperties.getDeliveryTag(), false);
    }
}
