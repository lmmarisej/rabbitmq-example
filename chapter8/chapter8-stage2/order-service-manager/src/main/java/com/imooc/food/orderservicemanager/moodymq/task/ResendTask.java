package com.imooc.food.orderservicemanager.moodymq.task;

import com.imooc.food.orderservicemanager.moodymq.po.TransMessagePO;
import com.imooc.food.orderservicemanager.moodymq.service.TransMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Configuration
@Component
@Slf4j
public class ResendTask {
    
    @Autowired
    TransMessageService transMessageService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Value("${moodymq.resendTimes}")
    Integer resendTimes;
    
    @Scheduled(fixedDelayString = "${moodymq.resendFreq}")
    public void resendMessage() {
        log.info("resendMessage() invoked.");
        List<TransMessagePO> messagePOS = transMessageService.listReadyMessages();
        log.info("resendMessage(): messagepos:{}", messagePOS);
        
        for (TransMessagePO po : messagePOS) {
            log.info("resendMessage(): po:{}", po);
            if (po.getSequence() > resendTimes) {           // 重发次数限制
                log.error("resend too many times!");
                transMessageService.messageDead(po.getId());
                continue;
            }
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            Message message = new Message(po.getPayload().getBytes(), messageProperties);
            message.getMessageProperties().setMessageId(po.getId());
            rabbitTemplate.convertAndSend(
                    po.getExchange(),
                    po.getRoutingKey(),
                    message,
                    new CorrelationData(po.getId()));
            
            log.info("message sent, ID:{}", po.getId());
            transMessageService.messageResend(po.getId());
        }
    }
}
