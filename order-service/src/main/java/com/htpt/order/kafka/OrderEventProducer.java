package com.htpt.order.kafka;

import com.htpt.common.constants.KafkaConstants;
import com.htpt.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderCreated(OrderEvent event) {
        log.info("Publishing ORDER_CREATED event: orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaConstants.ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
        log.info("ORDER_CREATED event published successfully");
    }

    public void publishOrderCancelled(OrderEvent event) {
        log.info("Publishing ORDER_CANCELLED event: orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaConstants.ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
        log.info("ORDER_CANCELLED event published successfully");
    }

    public void publishOrderPaid(OrderEvent event) {
        log.info("Publishing ORDER_PAID event: orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaConstants.ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
        log.info("ORDER_PAID event published successfully");
    }
}
