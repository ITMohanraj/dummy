package com.cropdeal.deliveryservice.config;

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

    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String DELIVERY_EVENTS_QUEUE = "delivery.events.queue";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public Queue deliveryEventsQueue() {
        return QueueBuilder.durable(DELIVERY_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingDeliveryEvents(Queue deliveryEventsQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryEventsQueue).to(deliveryExchange).with("delivery.#");
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
