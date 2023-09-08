package com.dlwlrma.IntelliBi.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {

    //普通交换机
    public static final String BI_EXCHANGE_NAME = "BiExchange";
    //普通队列
    public static final String BI_QUEUE_NAME = "BiQueue";
    //死信交换机
    public static final String BI_DEAD_EXCHANGE_NAME = "DeadExchange";
    //死信队列
    public static final String BI_DEAD_QUEUE_NAME = "DeadQueue";
    //routingKey
    public static final String BI_ROUTING_KEY = "BiKey";
    //死信routingKey
    public static final String BI_DEAD_ROUTING_KEY = "DeadBiKey";


    //创建普通交换机
    @Bean
    public DirectExchange biExchange() {
        return new DirectExchange(BI_EXCHANGE_NAME, true, false);
    }

    //创建死信交换机
    @Bean
    public DirectExchange biDeadExchange() {
        return new DirectExchange(BI_DEAD_EXCHANGE_NAME, true, false);
    }

    //创建普通队列  为用户的请求消息添加过期时间
    @Bean
    public Queue biQueue() {
        Map<String, Object> arguments = new HashMap<>();
        //为该队列添加死信交换机和死信队列
        //设置死信交换机
        arguments.put("x-dead-letter-exchange", BI_DEAD_EXCHANGE_NAME);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key", BI_DEAD_ROUTING_KEY);
        //设置TTL 60s 单位是ms 表示队列消息的过期时间为60s
        arguments.put("x-message-ttl", 60000);
        return QueueBuilder.durable(BI_QUEUE_NAME).withArguments(arguments).build();
    }

    //声明死信队列
    @Bean
    public Queue biDeadQueue() {
        return QueueBuilder.durable(BI_DEAD_QUEUE_NAME).build();
    }

    //绑定交换机与队列
    @Bean
    public Binding biBinding() {
        return BindingBuilder.bind(biQueue()).to(biExchange()).with(BI_ROUTING_KEY);
    }

    //绑定死信交换机与死信队列
    @Bean
    public Binding biDeadBinding() {
        return BindingBuilder.bind(biDeadQueue()).to(biDeadExchange()).with(BI_ROUTING_KEY);
    }
}
