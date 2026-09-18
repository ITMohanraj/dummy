package com.cropdeal.notificationservice.config;

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

    public static final String AUTH_EXCHANGE = "auth.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String CROP_EXCHANGE = "crop.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String NEGOTIATION_EXCHANGE = "negotiation.exchange";
    public static final String INVOICE_EXCHANGE = "invoice.exchange";

    public static final String PASSWORD_RESET_NOTIF_QUEUE = "notification.password-reset.queue";
    public static final String ORDER_NOTIF_QUEUE = "order.notification.queue";
    public static final String CROP_NOTIF_QUEUE = "crop.notification.queue";
    public static final String DELIVERY_NOTIF_QUEUE = "delivery.notification.queue";
    public static final String REVIEW_NOTIF_QUEUE = "review.notification.queue";
    public static final String NEGOTIATION_NOTIF_QUEUE = "negotiation.notification.queue";
    public static final String INVOICE_NOTIF_QUEUE = "invoice.notification.queue";

    @Bean
    public TopicExchange authTopicExchange() {
        return new TopicExchange(AUTH_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange orderTopicExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange cropTopicExchange() {
        return new TopicExchange(CROP_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deliveryTopicExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange reviewTopicExchange() {
        return new TopicExchange(REVIEW_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange negotiationTopicExchange() {
        return new TopicExchange(NEGOTIATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange invoiceTopicExchange() {
        return new TopicExchange(INVOICE_EXCHANGE, true, false);
    }

    @Bean
    public Queue passwordResetNotifQueue() {
        return QueueBuilder.durable(PASSWORD_RESET_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue orderNotifQueue() {
        return QueueBuilder.durable(ORDER_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue cropNotifQueue() {
        return QueueBuilder.durable(CROP_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue deliveryNotifQueue() {
        return QueueBuilder.durable(DELIVERY_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue reviewNotifQueue() {
        return QueueBuilder.durable(REVIEW_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue negotiationNotifQueue() {
        return QueueBuilder.durable(NEGOTIATION_NOTIF_QUEUE).build();
    }

    @Bean
    public Queue invoiceNotifQueue() {
        return QueueBuilder.durable(INVOICE_NOTIF_QUEUE).build();
    }

    @Bean
    public Binding bindingPasswordResetNotif(Queue passwordResetNotifQueue, TopicExchange authTopicExchange) {
        return BindingBuilder.bind(passwordResetNotifQueue).to(authTopicExchange).with("auth.password-reset.#");
    }

    @Bean
    public Binding bindingOrderNotif(Queue orderNotifQueue, TopicExchange orderTopicExchange) {
        return BindingBuilder.bind(orderNotifQueue).to(orderTopicExchange).with("order.#");
    }

    @Bean
    public Binding bindingCropNotif(Queue cropNotifQueue, TopicExchange cropTopicExchange) {
        return BindingBuilder.bind(cropNotifQueue).to(cropTopicExchange).with("crop.#");
    }

    @Bean
    public Binding bindingDeliveryNotif(Queue deliveryNotifQueue, TopicExchange deliveryTopicExchange) {
        return BindingBuilder.bind(deliveryNotifQueue).to(deliveryTopicExchange).with("delivery.#");
    }

    @Bean
    public Binding bindingReviewNotif(Queue reviewNotifQueue, TopicExchange reviewTopicExchange) {
        return BindingBuilder.bind(reviewNotifQueue).to(reviewTopicExchange).with("review.#");
    }

    @Bean
    public Binding bindingNegotiationNotif(Queue negotiationNotifQueue, TopicExchange negotiationTopicExchange) {
        return BindingBuilder.bind(negotiationNotifQueue).to(negotiationTopicExchange).with("negotiation.#");
    }

    @Bean
    public Binding bindingInvoiceNotif(Queue invoiceNotifQueue, TopicExchange invoiceTopicExchange) {
        return BindingBuilder.bind(invoiceNotifQueue).to(invoiceTopicExchange).with("invoice.#");
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
