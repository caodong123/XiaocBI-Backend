package com.yupi.springbootinit.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        //创建队列
        channel.queueDeclare("xiaoC", true, false, false, null);
        //绑定
        channel.queueBind("xiaoC", EXCHANGE_NAME, "xiaoC");

        //创建队列
        channel.queueDeclare("xiaoD", true, false, false, null);
        //绑定
        channel.queueBind("xiaoD", EXCHANGE_NAME, "xiaoD");


        DeliverCallback xiaoCdeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaoC] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume("xiaoC", true, xiaoCdeliverCallback, consumerTag -> {
        });

        DeliverCallback xiaoDdeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [xiaoD] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume("xiaoD", true, xiaoDdeliverCallback, consumerTag -> {
        });
    }
}