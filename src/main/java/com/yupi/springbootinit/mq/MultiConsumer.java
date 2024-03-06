package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {
    //声明队列名称
    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        //创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();


        for (int i = 1; i <= 2; i++) {
            final Channel channel = connection.createChannel();
            //创建对列
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            //积压消息数   设置为1 意思是必须等你执行确认完 消息队列才能给你下一个消息
            channel.basicQos(1);
            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    System.out.println(" 消费者编号 " + finalI + ",接收消息" + message + "'");
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    //失败 不设置ack
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    throw new RuntimeException(e);
                } finally {
                    System.out.println(" [x] Done");
                    //ack确认
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }


    }


}