package com.cropdeal.orderservice.config;

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

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String NEGOTIATION_EXCHANGE = "negotiation.exchange";
    public static final String BIDDING_EXCHANGE = "bidding.exchange";

    public static final String ORDER_EVENTS_QUEUE = "order.events.queue";
    public static final String ORDER_NEGOTIATION_QUEUE = "order.negotiation.queue";
    public static final String ORDER_BIDDING_QUEUE = "order.bidding.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange negotiationExchange() {
        return new TopicExchange(NEGOTIATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange biddingExchange() {
        return new TopicExchange(BIDDING_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderEventsQueue() {
        return QueueBuilder.durable(ORDER_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue orderNegotiationQueue() {
        return QueueBuilder.durable(ORDER_NEGOTIATION_QUEUE).build();
    }

    @Bean
    public Queue orderBiddingQueue() {
        return QueueBuilder.durable(ORDER_BIDDING_QUEUE).build();
    }

    @Bean
    public Binding bindingOrderEvents(Queue orderEventsQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderEventsQueue).to(orderExchange).with("order.#");
    }

    @Bean
    public Binding bindingOrderNegotiation(Queue orderNegotiationQueue, TopicExchange negotiationExchange) {
        return BindingBuilder.bind(orderNegotiationQueue).to(negotiationExchange).with("negotiation.accepted");
    }

    @Bean
    public Binding bindingOrderBidding(Queue orderBiddingQueue, TopicExchange biddingExchange) {
        return BindingBuilder.bind(orderBiddingQueue).to(biddingExchange).with("bid.accepted");
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