package com.imooc.food.rewardservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.rewardservicemanager.dao.RewardDao;
import com.imooc.food.rewardservicemanager.dto.OrderMessageDTO;
import com.imooc.food.rewardservicemanager.enummeration.RewardStatus;
import com.imooc.food.rewardservicemanager.po.RewardPO;
import com.imooc.moodymq.listener.AbstractMessageListener;
import com.imooc.moodymq.sender.TransMessageSender;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService extends AbstractMessageListener {

    @Autowired
    RewardDao rewardDao;
    @Autowired
    private TransMessageSender transMessageSender;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void receiveMessage(Message message) {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);
            log.info("handleOrderService:orderSettlementDTO:{}", orderMessageDTO);
            //业务代码
            RewardPO rewardPO = new RewardPO();
            rewardPO.setOrderId(orderMessageDTO.getOrderId());
            rewardPO.setStatus(RewardStatus.SUCCESS);
            rewardPO.setAmount(orderMessageDTO.getPrice());
            rewardPO.setDate(new Date());
            rewardDao.insert(rewardPO);
            orderMessageDTO.setRewardId(rewardPO.getId());

            transMessageSender.send(
                    "exchange.order.settlement",
                    "key.settlement",
                    orderMessageDTO
            );
        } catch (JsonProcessingException  e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}

