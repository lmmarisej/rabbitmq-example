package com.imooc.moodymq.task;

import com.imooc.moodymq.po.TransMessagePO;
import com.imooc.moodymq.service.TransMessageService;
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

    @Value("#{new Integer('${moodymq.resendTimes}')}")
    int resendTimes;

    @Scheduled(fixedDelayString = "${moodymq.resendFreq}")
    public void resendMessage() {
        log.info("resendMessage()");
        List<TransMessagePO> messagePOS = transMessageService.listReadyMessages();
        log.info("resendMessage():messagePOS:{}", messagePOS);
        for (TransMessagePO messagePO : messagePOS) {
            log.info("resendMessage(): messagePO:{}", messagePO);
            if (messagePO.getSequence() > resendTimes) {
                log.error("message resend too many times! transMessagePO:{}", messagePO);
                transMessageService.messageDead(messagePO.getId());
                continue;
            }
            //设置消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType("application/json");
            Message message = new Message(messagePO.getPayload().getBytes(), messageProperties);
            message.getMessageProperties().setMessageId(messagePO.getId());
            //发送
            rabbitTemplate.convertAndSend(messagePO.getExchange(), messagePO.getRoutingKey(),
                    message, new CorrelationData(messagePO.getId()));
            transMessageService.messageResend(messagePO.getId());
        }
    }
}
