package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TtlProducer {
    //声明队列名称
    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置ip地址
        factory.setHost("localhost");
        //未修改的话就是默认的 可以不填
//        factory.setHost();
//        factory.setUsername();
//        factory.setPassword();
        try (Connection connection = factory.newConnection();
             //创建队列
             Channel channel = connection.createChannel()) {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-message-ttl", 1000);
            channel.queueDeclare(QUEUE_NAME, false, false, false, args);
            //传送的消息
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}