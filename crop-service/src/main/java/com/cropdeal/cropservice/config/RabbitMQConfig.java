package com.cropdeal.cropservice.config;

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

    public static final String CROP_EXCHANGE = "crop.exchange";
    public static final String CROP_EVENTS_QUEUE = "crop.events.queue";

    @Bean
    public TopicExchange cropExchange() {
        return new TopicExchange(CROP_EXCHANGE, true, false);
    }

    @Bean
    public Queue cropEventsQueue() {
        return QueueBuilder.durable(CROP_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingCropEvents(Queue cropEventsQueue, TopicExchange cropExchange) {
        return BindingBuilder.bind(cropEventsQueue).to(cropExchange).with("crop.#");
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
