package com.cropdeal.negotiationservice.config;

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

    public static final String NEGOTIATION_EXCHANGE = "negotiation.exchange";
    public static final String NEGOTIATION_EVENTS_QUEUE = "negotiation.events.queue";

    @Bean
    public TopicExchange negotiationExchange() {
        return new TopicExchange(NEGOTIATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue negotiationEventsQueue() {
        return QueueBuilder.durable(NEGOTIATION_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingNegotiationEvents(Queue negotiationEventsQueue, TopicExchange negotiationExchange) {
        return BindingBuilder.bind(negotiationEventsQueue).to(negotiationExchange).with("negotiation.#");
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