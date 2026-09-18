package com.cropdeal.reportservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REPORT_QUEUE = "report.events.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String CROP_EXCHANGE = "crop.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(REPORT_QUEUE).build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange cropExchange() {
        return new TopicExchange(CROP_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindReportToOrder(Queue reportQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(reportQueue).to(orderExchange).with("order.#");
    }

    @Bean
    public Binding bindReportToCrop(Queue reportQueue, TopicExchange cropExchange) {
        return BindingBuilder.bind(reportQueue).to(cropExchange).with("crop.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
