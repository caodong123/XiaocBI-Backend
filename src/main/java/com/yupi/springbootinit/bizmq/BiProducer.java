package com.yupi.springbootinit.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息发送者
 */
@Slf4j
@Component
public class BiProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 交换机和路由已经被限定 只需要传递消息即可
     * @param message 消息
     */
    public void sendMessage(String message){
        //使用rabbitTemplate的convertAndSend方法指定交换机和路由 发送消息
        rabbitTemplate.convertAndSend(BiConstant.EXCHANGE_NAME,BiConstant.BI_ROUTING_KEY,message);
        log.info("成功发送消息:"+message);
    }

}
