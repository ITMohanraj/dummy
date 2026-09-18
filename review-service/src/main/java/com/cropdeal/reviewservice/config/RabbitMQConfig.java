package com.cropdeal.reviewservice.config;

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

    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String REVIEW_EVENTS_QUEUE = "review.events.queue";

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange(REVIEW_EXCHANGE, true, false);
    }

    @Bean
    public Queue reviewEventsQueue() {
        return QueueBuilder.durable(REVIEW_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingReviewEvents(Queue reviewEventsQueue, TopicExchange reviewExchange) {
        return BindingBuilder.bind(reviewEventsQueue).to(reviewExchange).with("review.#");
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
