package com.cropdeal.invoiceservice.config;

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

    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVOICE_ORDER_QUEUE = "invoice.order.events.queue";
    public static final String INVOICE_EVENTS_QUEUE = "invoice.events.queue";

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue invoiceOrderQueue() {
        return QueueBuilder.durable(INVOICE_ORDER_QUEUE).build();
    }

    @Bean
    public Queue invoiceEventsQueue() {
        return QueueBuilder.durable(INVOICE_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding bindingInvoiceOrderEvents(Queue invoiceOrderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(invoiceOrderQueue).to(orderExchange).with("order.#");
    }

    @Bean
    public Binding bindingInvoiceEvents(Queue invoiceEventsQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(invoiceEventsQueue).to(invoiceExchange).with("invoice.#");
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