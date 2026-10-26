package com.cfs.FlowPay.kafka;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka-enabled", havingValue = "true")
public class EventListener {
    private final EventConsumerService service;

    public EventListener(EventConsumerService s) {
        service = s;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC)
    public void onEvent(String eventId) {
        service.consume(eventId);
    }
}