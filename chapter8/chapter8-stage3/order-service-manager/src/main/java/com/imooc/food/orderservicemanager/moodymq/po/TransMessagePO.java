package com.imooc.food.orderservicemanager.moodymq.po;

import com.imooc.food.orderservicemanager.moodymq.enummeration.TransMessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class TransMessagePO {
    private String id;
    private String service;
    private TransMessageType type;
    private String exchange;
    private String routingKey;
    private String queue;
    private Integer sequence;
    private String payload;
    private Date date;
}
