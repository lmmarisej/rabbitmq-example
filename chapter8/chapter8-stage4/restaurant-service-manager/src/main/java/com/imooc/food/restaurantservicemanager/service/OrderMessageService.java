package com.imooc.food.restaurantservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.restaurantservicemanager.dao.ProductDao;
import com.imooc.food.restaurantservicemanager.dao.RestaurantDao;
import com.imooc.food.restaurantservicemanager.dto.OrderMessageDTO;
import com.imooc.food.restaurantservicemanager.enummeration.ProductStatus;
import com.imooc.food.restaurantservicemanager.enummeration.RestaurantStatus;
import com.imooc.food.restaurantservicemanager.po.ProductPO;
import com.imooc.food.restaurantservicemanager.po.RestaurantPO;
import com.imooc.moodymq.listener.AbstractMessageListener;
import com.imooc.moodymq.sender.TransMessageSender;
import com.rabbitmq.client.Return;
import com.rabbitmq.client.ReturnCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderMessageService extends AbstractMessageListener {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProductDao productDao;
    @Autowired
    RestaurantDao restaurantDao;
    @Autowired
    private TransMessageSender transMessageSender;

    @Override
    public void receiveMessage(Message message) {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);

            ProductPO productPO = productDao.selsctProduct(orderMessageDTO.getProductId());
            log.info("onMessage:productPO:{}", productPO);
            RestaurantPO restaurantPO = restaurantDao.selsctRestaurant(productPO.getRestaurantId());
            log.info("onMessage:restaurantPO:{}", restaurantPO);
            if (ProductStatus.AVALIABLE == productPO.getStatus() && RestaurantStatus.OPEN == restaurantPO.getStatus()) {
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }
            log.info("sendMessage:restaurantOrderMessageDTO:{}", orderMessageDTO);

            transMessageSender.send(
                    "exchange.order.restaurant",
                    "key.order",
                    orderMessageDTO
            );

        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}

