package com.htpt.notification.kafka;

import com.htpt.common.constants.KafkaConstants;
import com.htpt.common.event.OrderEvent;
import com.htpt.common.event.InventoryEvent;
import com.htpt.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaConstants.ORDER_EVENTS_TOPIC,
            groupId = KafkaConstants.NOTIFICATION_ORDER_GROUP,
            properties = {"spring.json.value.default.type=com.htpt.common.event.OrderEvent"}
    )
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        String title;
        String message;

        switch (event.getEventType()) {
            case KafkaConstants.ORDER_CREATED -> {
                title = "Order Created";
                message = String.format("Your order #%d has been placed successfully. Total: ₫%s",
                        event.getOrderId(), event.getTotalAmount().multiply(java.math.BigDecimal.valueOf(1000)));
            }
            case KafkaConstants.ORDER_PAID -> {
                title = "Payment Successful";
                message = String.format("Payment for order #%d was successful. Total: ₫%s",
                        event.getOrderId(), event.getTotalAmount().multiply(java.math.BigDecimal.valueOf(1000)));
            }
            case KafkaConstants.ORDER_CANCELLED -> {
                title = "Order Cancelled";
                message = String.format("Your order #%d has been cancelled. Reason: %s",
                        event.getOrderId(), event.getReason());
            }
            default -> {
                log.warn("Unknown order event type: {}", event.getEventType());
                return;
            }
        }

        notificationService.createNotification(event.getUserId(), event.getEventType(), title, message);
    }

    @KafkaListener(
            topics = KafkaConstants.INVENTORY_EVENTS_TOPIC,
            groupId = KafkaConstants.NOTIFICATION_INVENTORY_GROUP,
            properties = {"spring.json.value.default.type=com.htpt.common.event.InventoryEvent"}
    )
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event: productId={}, stock={}", event.getProductId(), event.getNewStock());

        if (event.getNewStock() <= 5) {
            String title = "Low Stock Alert";
            String message = String.format("Product '%s' is running low on stock: %d remaining",
                    event.getProductName(), event.getNewStock());
            // Notify admin user (userId=1 as convention for admin)
            notificationService.createNotification(1L, "LOW_STOCK", title, message);
        }
    }
}
