package com.yupi.springbootinit.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 消息发送者
 */
@Slf4j
@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param exchange 交换机
     * @param routingKey 路由
     * @param message 消息
     */
    public void sendMessage(String exchange,String routingKey,String message){
        //使用rabbitTemplate的convertAndSend方法指定交换机和路由 发送消息
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        log.info("成功发送消息:"+message);
    }

}
