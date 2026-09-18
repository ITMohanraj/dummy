package com.cropdeal.reportservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportEventListener {

    @RabbitListener(queues = "report.events.queue")
    public void onReportEvent(Map<String, Object> payload) {
        log.info("Report Service updating CQRS aggregates for event: {}", payload);
    }
}
