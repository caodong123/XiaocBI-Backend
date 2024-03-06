package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class FanoutConsumer {
  //声明交换机名字
  private static final String EXCHANGE_NAME = "fanout-exchanger";


  public static void main(String[] argv) throws Exception {
    //创建连接
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Channel channel2 = connection.createChannel();
    //创建交换机和队列
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    //队列名字
    String queueName = "小王的队列";
    String queueName2 = "小李的队列";
    //创建对列
    channel.queueDeclare(queueName,true,false,false,null);
    channel.queueDeclare(queueName2,true,false,false,null);
    //绑定交换机和队列
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    channel.queueBind(queueName2, EXCHANGE_NAME, "");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        //ack确认
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
        System.out.println(" [小王] Received '" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      //ack确认
      channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
      System.out.println(" [小李] Received '" + message + "'");
    };
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}