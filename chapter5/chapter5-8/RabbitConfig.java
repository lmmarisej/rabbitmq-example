package com.imooc.food.orderservicemanager.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.food.orderservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;


    /*---------------------restaurant---------------------*/
//    @Bean
//    public Exchange exchange1() {
//        return new DirectExchange("exchange.order.restaurant");
//    }
//
//    @Bean
//    public Queue queue1() {
//        return new Queue("queue.order");
//    }
//
//    @Bean
//    public Binding binding1() {
//        return new Binding(
//                "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.restaurant",
//                "key.order",
//                null);
//    }
//
//    /*---------------------deliveryman---------------------*/
//    @Bean
//    public Exchange exchange2() {
//        return new DirectExchange("exchange.order.deliveryman");
//    }
//
//    @Bean
//    public Binding binding2() {
//        return new Binding(
//                "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.deliveryman",
//                "key.order",
//                null);
//    }
//
//
//    /*---------settlement---------*/
//    @Bean
//    public Exchange exchange3() {
//        return new FanoutExchange("exchange.order.settlement");
//    }
//
//    @Bean
//    public Exchange exchange4() {
//        return new FanoutExchange("exchange.settlement.order");
//    }
//
//    @Bean
//    public Binding binding3() {
//        return new Binding(
//                "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.settlement.order",
//                "key.order",
//                null);
//    }
//
//    /*--------------reward----------------*/
//    @Bean
//    public Exchange exchange5() {
//        return new TopicExchange("exchange.order.reward");
//    }
//
//    @Bean
//    public Binding binding4() {
//        return new Binding(
//                "queue.order",
//                Binding.DestinationType.QUEUE,
//                "exchange.order.reward",
//                "key.order",
//                null);
//    }

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost("127.0.0.1");
//        connectionFactory.setPort(5672);
//        connectionFactory.setPassword("guest");
//        connectionFactory.setUsername("guest");
//        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
//        connectionFactory.setPublisherReturns(true);
//        connectionFactory.createConnection();
//        return connectionFactory;
//    }
//
//    @Bean
//    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
//        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
//        admin.setAutoStartup(true);
//        return admin;
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMandatory(true);
//        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
//            log.info("message:{}, replyCode:{}, replyText:{}, exchange:{}, routingKey:{}",
//                    message, replyCode, replyText, exchange, routingKey);
//        });
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) ->
//                log.info("correlationData:{}, ack:{}, cause:{}",
//                        correlationData, ack, cause));
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public RabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        return factory;
//    }

//    @Bean
//    public SimpleMessageListenerContainer messageListenerContainer(
//            @Autowired ConnectionFactory connectionFactory
//    ) {
//        SimpleMessageListenerContainer messageListenerContainer =
//                new SimpleMessageListenerContainer(connectionFactory);
//        messageListenerContainer.setQueueNames("queue.order");
//        messageListenerContainer.setConcurrentConsumers(3);
//        messageListenerContainer.setMaxConcurrentConsumers(5);
//        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
//        //        messageListenerContainer.setMessageListener(new MessageListener() {
//        //            @Override
//        //            public void onMessage(Message message) {
//        //                log.info("message:{}", message);
//        //            }
//        //        });
////        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
////                        messageListenerContainer.setMessageListener(new ChannelAwareMessageListener() {
////                            @Override
////                            public void onMessage(Message message, Channel channel) throws Exception {
////                                log.info("message:{}", message);
////                               ObjectMapper objectMapper =  new ObjectMapper();
////                                objectMapper.readValue(message.getBody() ,OrderMessageDTO);
////                                orderMessageService.handleMessage();
////                                channel.basicAck(
////                                        message.getMessageProperties().getDeliveryTag(),
////                                        false
////                                );
////                            }
////                        });
//        messageListenerContainer.setPrefetchCount(1);
//
//        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(orderMessageService);
//        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();
//        messageConverter.setClassMapper(new ClassMapper() {
//            @Override
//            public void fromClass(Class<?> clazz, MessageProperties properties) {
//
//            }
//
//            @Override
//            public Class<?> toClass(MessageProperties properties) {
//                return OrderMessageDTO.class;
//            }
//        });
////        Jackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
////        messageConverter.setJavaTypeMapper();
////        ContentTypeDelegatingMessageConverter delegatingMessageConverter =
////                new ContentTypeDelegatingMessageConverter();
//        messageListenerAdapter.setMessageConverter(messageConverter);
//        messageListenerContainer.setMessageListener(messageListenerAdapter);
//
//        return messageListenerContainer;
//    }
}
