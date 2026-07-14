package com.htpt.order.service;

import com.htpt.common.exception.BadRequestException;
import com.htpt.common.exception.ResourceNotFoundException;
import com.htpt.common.event.OrderEvent;
import com.htpt.order.client.ProductClient;
import com.htpt.order.client.UserClient;
import com.htpt.order.dto.*;
import com.htpt.order.kafka.OrderEventProducer;
import com.htpt.order.model.Order;
import com.htpt.order.model.OrderItem;
import com.htpt.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final OrderEventProducer eventProducer;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for userId={}, items={}", userId, request.getItems().size());

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderEvent.OrderItemEvent> eventItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // REST call to Product Service (synchronous)
            ProductClient.ProductDto product;
            try {
                var response = productClient.getProduct(itemReq.getProductId());
                product = response.getData();
            } catch (Exception e) {
                log.error("Failed to fetch product id={}: {}", itemReq.getProductId(), e.getMessage());
                throw new BadRequestException("Product with id " + itemReq.getProductId() + " not found or unavailable");
            }

            if (product == null || !product.active()) {
                throw new BadRequestException("Product with id " + itemReq.getProductId() + " is not available");
            }

            BigDecimal itemTotal = product.price().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .quantity(itemReq.getQuantity())
                    .price(product.price())
                    .build();
            order.addItem(orderItem);

            eventItems.add(OrderEvent.OrderItemEvent.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .quantity(itemReq.getQuantity())
                    .price(product.price())
                    .build());
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);
        log.info("Order created: id={}, total={}", order.getId(), total);

        // Publish Kafka event (asynchronous)
        OrderEvent event = OrderEvent.orderCreated(order.getId(), userId, eventItems, total);
        eventProducer.publishOrderCreated(event);

        return OrderResponse.fromEntity(order);
    }

    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        log.info("Fetching orders for userId={}", userId);
        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponse::fromEntity);
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        log.info("Fetching order id={} for userId={}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Not authorized to view this order");
        }
        
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order id={} status to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }

        order = orderRepository.save(order);
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order id={} by userId={}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Not authorized to cancel this order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        // Refund if already paid
        if (order.getStatus() == Order.OrderStatus.PAID) {
            BigDecimal amountInVnd = order.getTotalAmount().multiply(BigDecimal.valueOf(1000));
            try {
                userClient.refundBalance(userId, Map.of("amount", amountInVnd), "htpt-internal-secret-2024");
            } catch (Exception e) {
                log.error("Failed to refund balance for user id={}: {}", userId, e.getMessage());
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Publish cancel event → Inventory restores stock
        List<OrderEvent.OrderItemEvent> eventItems = order.getItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        OrderEvent event = OrderEvent.orderCancelled(orderId, userId, eventItems, "User cancelled");
        eventProducer.publishOrderCancelled(event);
        log.info("Order cancelled: id={}", orderId);
    }

    @Transactional
    public OrderResponse payOrder(Long orderId, Long userId) {
        log.info("Paying for order id={}, userId={}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Not authorized to pay for this order");
        }

        if (order.getStatus() != Order.OrderStatus.CREATED) {
            throw new BadRequestException("Order is not in CREATED status");
        }

        // 1 USD = 1000 VND
        BigDecimal amountInVnd = order.getTotalAmount().multiply(BigDecimal.valueOf(1000));

        // Deduct balance
        try {
            userClient.deductBalance(userId, Map.of("amount", amountInVnd), "htpt-internal-secret-2024");
        } catch (Exception e) {
            log.error("Failed to deduct balance for user id={}: {}", userId, e.getMessage());
            throw new BadRequestException("Payment failed: Insufficient balance or user service unavailable");
        }

        order.setStatus(Order.OrderStatus.PAID);
        order.setPaidAt(java.time.LocalDateTime.now());
        order = orderRepository.save(order);

        // Publish event
        List<OrderEvent.OrderItemEvent> eventItems = order.getItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();
        OrderEvent event = OrderEvent.orderPaid(orderId, userId, eventItems, order.getTotalAmount());
        eventProducer.publishOrderPaid(event);

        return OrderResponse.fromEntity(order);
    }
}
