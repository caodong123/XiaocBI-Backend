package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.io.SAXReader;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MyMessageConsumer {

    /**
     *
     * @param message 接收的消息
     * @param channel 连接
     * @param deliverTag 消息的id
     * @throws IOException
     */
    //queues指定监听的队列
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        log.info("收到消息:"+message);
        //手动确认ack
        channel.basicAck(deliverTag,false);
    }

}
