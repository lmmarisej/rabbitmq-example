package com.imooc.food.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.orderservicemanager.dao.OrderDetailDao;
import com.imooc.food.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.food.orderservicemanager.enummeration.OrderStatus;
import com.imooc.food.orderservicemanager.po.OrderDetailPO;
import com.imooc.food.orderservicemanager.vo.OrderCreateVO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * 演示 MQ 确认消息：单条、多条同步确认 + 异步确认
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderDetailDao orderDetailDao;
    @Autowired
    RabbitTemplate rabbitTemplate;

    ObjectMapper objectMapper = new ObjectMapper();


    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
        log.info("createOrder:orderCreateVO:{}", orderCreateVO);
        OrderDetailPO orderPO = new OrderDetailPO();
        orderPO.setAddress(orderCreateVO.getAddress());
        orderPO.setAccountId(orderCreateVO.getAccountId());
        orderPO.setProductId(orderCreateVO.getProductId());
        orderPO.setStatus(OrderStatus.ORDER_CREATING);
        orderPO.setDate(new Date());
        orderDetailDao.insert(orderPO);

        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderPO.getId());
        orderMessageDTO.setProductId(orderPO.getProductId());
        orderMessageDTO.setAccountId(orderCreateVO.getAccountId());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            channel.confirmSelect();        // 开启消息确认机制
            channel.addConfirmListener(new ConfirmListener() {
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                   log.info("Ack, deliveryTag: {}, multiple: {}",  deliveryTag, multiple);
                }
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    log.info("Nack, deliveryTag: {}, multiple: {}", deliveryTag, multiple);
                }
            });
            for (int i = 0; i < 10; i++) {
                channel.basicPublish("exchange.order.restaurant", "key.restaurant", null, messageToSend.getBytes());
                log.info("message sent");
            }
//            if(channel.waitForConfirms()){
//                log.info("confirm OK");
//            }else {
//                log.info("confirm Failed");
//            }
            Thread.sleep(100000);
        }
    }

}
