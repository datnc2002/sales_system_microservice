package com.htpt.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends BaseEvent {

    private Long orderId;
    private Long userId;
    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;
    private String status;
    private String reason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }

    public static OrderEvent orderCreated(Long orderId, Long userId,
                                           List<OrderItemEvent> items, BigDecimal totalAmount) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .totalAmount(totalAmount)
                .status("CREATED")
                .build();
        event.initEvent("ORDER_CREATED");
        return event;
    }

    public static OrderEvent orderCancelled(Long orderId, Long userId,
                                             List<OrderItemEvent> items, String reason) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .reason(reason)
                .status("CANCELLED")
                .build();
        event.initEvent("ORDER_CANCELLED");
        return event;
    }

    public static OrderEvent orderPaid(Long orderId, Long userId,
                                             List<OrderItemEvent> items, BigDecimal totalAmount) {
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .totalAmount(totalAmount)
                .status("PAID")
                .build();
        event.initEvent("ORDER_PAID");
        return event;
    }
}
