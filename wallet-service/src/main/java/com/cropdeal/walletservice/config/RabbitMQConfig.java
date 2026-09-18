package com.cropdeal.walletservice.config;

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

    public static final String WALLET_EXCHANGE = "wallet.exchange";
    public static final String WALLET_EVENTS_QUEUE = "wallet.events.queue";

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE, true, false);
    }

    @Bean
    public Queue walletEventsQueue() {
        return QueueBuilder.durable(WALLET_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingWalletEvents(Queue walletEventsQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(walletEventsQueue).to(walletExchange).with("wallet.#");
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
