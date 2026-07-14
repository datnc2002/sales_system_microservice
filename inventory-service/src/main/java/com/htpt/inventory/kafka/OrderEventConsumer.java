package com.htpt.inventory.kafka;

import com.htpt.common.constants.KafkaConstants;
import com.htpt.common.event.OrderEvent;
import com.htpt.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR
    )
    @KafkaListener(
            topics = KafkaConstants.ORDER_EVENTS_TOPIC,
            groupId = KafkaConstants.INVENTORY_GROUP
    )
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case KafkaConstants.ORDER_CREATED -> {
                inventoryService.deductStock(event);
                log.info("Stock deducted for orderId={}", event.getOrderId());
            }
            case KafkaConstants.ORDER_CANCELLED -> {
                inventoryService.restoreStock(event);
                log.info("Stock restored for cancelled orderId={}", event.getOrderId());
            }
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
