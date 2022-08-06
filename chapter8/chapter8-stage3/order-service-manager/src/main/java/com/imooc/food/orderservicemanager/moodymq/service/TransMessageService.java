package com.imooc.food.orderservicemanager.moodymq.service;

import com.imooc.food.orderservicemanager.moodymq.po.TransMessagePO;

import java.util.List;

public interface TransMessageService {
    /**
     * 发送前暂存消息
     *
     * @param exchange exchange
     * @param routingKey routingKey
     * @param body body
     * @return TransMessagePO
     */
    TransMessagePO messageSendReady(String exchange, String routingKey, String body);

    /**
     * 设置消息发送成功
     *
     * @param id 消息ID
     */
    void messageSendSuccess(String id);

    /**
     * 设置消息返回
     * @param id id
     * @param exchange  exchange
     * @param routingKey routingKey
     * @param body body
     * @return TransMessagePO
     */
    TransMessagePO messageSendReturn(
            String id, String exchange, String routingKey, String body);

    /**
     * 查询应发未发消息
     * @return List<TransMessagePO>
     */
    List<TransMessagePO> listReadyMessages();

    /**
     * 记录消息发送次数
     * @param id id
     */
    void messageResend(String id);

    /**
     * 消息重发多次，放弃
     * @param id id
     */
    void messageDead(String id);

    /**
     * 保存监听到的死信消息
     * @param id
     * @param exchange
     * @param routingKey
     * @param queue
     * @param body
     */
    void messageDead(String id, String exchange,
                     String routingKey, String queue,
                     String body);

    /**
     * 消息消费前保存
     *
     * @param id
     * @param exchange
     * @param routingKey
     * @param queue
     * @param body
     * @return
     */
    TransMessagePO messageReceiveReady(
            String id,
            String exchange,
            String routingKey,
            String queue,
            String body);

    /**
     * 消息消费成功
     * @param id
     */
    void messageReceiveSuccess(String id);
}
