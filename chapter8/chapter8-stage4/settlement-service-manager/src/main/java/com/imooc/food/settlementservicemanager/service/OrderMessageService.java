package com.imooc.food.settlementservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.settlementservicemanager.dao.SettlementDao;
import com.imooc.food.settlementservicemanager.dto.OrderMessageDTO;
import com.imooc.food.settlementservicemanager.enummeration.SettlementStatus;
import com.imooc.food.settlementservicemanager.po.SettlementPO;
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

import javax.xml.stream.events.DTD;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService  extends AbstractMessageListener {

    @Autowired
    SettlementService settlementService;
    @Autowired
    SettlementDao settlementDao;
    @Autowired
    private TransMessageSender transMessageSender;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void receiveMessage(Message message) {
        String messageBody = new String(message.getBody());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);
            SettlementPO settlementPO = new SettlementPO();
            settlementPO.setAmount(orderMessageDTO.getPrice());
            settlementPO.setDate(new Date());
            settlementPO.setOrderId(orderMessageDTO.getOrderId());
            Integer transactionId = settlementService.settlement(
                    orderMessageDTO.getAccountId(),
                    orderMessageDTO.getPrice());
            settlementPO.setStatus(SettlementStatus.SUCCESS);
            settlementPO.setTransactionId(transactionId);
            settlementDao.insert(settlementPO);
            orderMessageDTO.setSettlementId(transactionId);
            transMessageSender.send(
                    "exchange.settlement.order",
                    "key.order",
                    orderMessageDTO
            );
        } catch(Exception ex){
            log.error(ex.getMessage(), ex);
            throw new RuntimeException();
        }
    }

}

