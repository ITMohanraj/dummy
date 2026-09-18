package com.cropdeal.biddingservice.config;

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

    public static final String BIDDING_EXCHANGE = "bidding.exchange";
    public static final String BIDDING_EVENTS_QUEUE = "bidding.events.queue";

    @Bean
    public TopicExchange biddingExchange() {
        return new TopicExchange(BIDDING_EXCHANGE, true, false);
    }

    @Bean
    public Queue biddingEventsQueue() {
        return QueueBuilder.durable(BIDDING_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingBiddingEvents(Queue biddingEventsQueue, TopicExchange biddingExchange) {
        return BindingBuilder.bind(biddingEventsQueue).to(biddingExchange).with("bid.#");
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
