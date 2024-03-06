package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class TopicProducer {

    private static final String EXCHANGE_NAME = "topic-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            Scanner sc = new Scanner(System.in);
            //一行为一个消息 方便连续发送消息
            while (sc.hasNext()) {
                String userInput = sc.nextLine();
                String[] strings = userInput.split(" ");
                if (strings.length < 2) {
                    continue;
                }
                String message = strings[0];
                String routingKey = strings[1];

                channel.basicPublish(EXCHANGE_NAME, routingKey,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'to" + routingKey);


            }
        }

    }
}