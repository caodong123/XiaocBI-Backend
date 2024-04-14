package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//初始化 创建交换机和队列
public class BiInit {

    public static void main(String[] args) throws IOException, TimeoutException {
        //创建交换机和队列
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置ip地址
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //创建交换机 direct交换机
        channel.exchangeDeclare(BiConstant.EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //创建队列
        channel.queueDeclare(BiConstant.QUEUE_NAME, true, false, false, null);
        //建立绑定
        channel.queueBind(BiConstant.QUEUE_NAME, BiConstant.EXCHANGE_NAME, BiConstant.BI_ROUTING_KEY);
        System.out.println("ok");
    }
}
