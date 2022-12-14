package com.imooc.food.orderservicemanager.moodymq.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.orderservicemanager.moodymq.po.TransMessagePO;
import com.imooc.food.orderservicemanager.moodymq.service.TransMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransMessageSender {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    TransMessageService transMessageService;

    public void send(String exchange, String routingKey, Object payload){
        log.info("send(): exchange:{} routingKey:{} payload:{}",
                exchange, routingKey, payload);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String payloadStr = mapper.writeValueAsString(payload);

            TransMessagePO transMessagePO =
                    transMessageService.messageSendReady(
                    exchange,
                    routingKey,
                    payloadStr
            );

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            Message message = new Message(payloadStr.getBytes(), messageProperties);
            message.getMessageProperties().setMessageId(transMessagePO.getId());
            rabbitTemplate.convertAndSend(exchange, routingKey, message,
                    new CorrelationData(transMessagePO.getId()));

            log.info("message sent, ID:{}", transMessagePO.getId());

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }


    }
}
