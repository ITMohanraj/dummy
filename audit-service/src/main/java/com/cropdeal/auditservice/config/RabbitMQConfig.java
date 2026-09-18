package com.cropdeal.auditservice.config;

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

    public static final String AUDIT_QUEUE = "audit.events.queue";
    public static final String AUDIT_EXCHANGE = "audit.exchange";
    public static final String AUTH_EXCHANGE = "auth.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String CROP_EXCHANGE = "crop.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String NEGOTIATION_EXCHANGE = "negotiation.exchange";
    public static final String INVOICE_EXCHANGE = "invoice.exchange";

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
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
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange negotiationExchange() {
        return new TopicExchange(NEGOTIATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindAuditToAuth(Queue auditQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(auditQueue).to(authExchange).with("#");
    }

    @Bean
    public Binding bindAuditToOrder(Queue auditQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(auditQueue).to(orderExchange).with("#");
    }

    @Bean
    public Binding bindAuditToCrop(Queue auditQueue, TopicExchange cropExchange) {
        return BindingBuilder.bind(auditQueue).to(cropExchange).with("#");
    }

    @Bean
    public Binding bindAuditToPayment(Queue auditQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(auditQueue).to(paymentExchange).with("#");
    }

    @Bean
    public Binding bindAuditToDelivery(Queue auditQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(auditQueue).to(deliveryExchange).with("#");
    }

    @Bean
    public Binding bindAuditToNegotiation(Queue auditQueue, TopicExchange negotiationExchange) {
        return BindingBuilder.bind(auditQueue).to(negotiationExchange).with("#");
    }

    @Bean
    public Binding bindAuditToInvoice(Queue auditQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(auditQueue).to(invoiceExchange).with("#");
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
