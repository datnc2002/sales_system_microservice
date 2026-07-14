package com.htpt.order.controller;

import com.htpt.common.dto.ApiResponse;
import com.htpt.order.dto.*;
import com.htpt.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders - userId={}", userId);
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/orders - userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success(orderService.getUserOrders(userId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("GET /api/orders/{} - userId={}", id, userId);
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id, userId)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("PUT /api/orders/{}/status", id);
        OrderResponse response = orderService.updateOrderStatus(id, body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("DELETE /api/orders/{} - userId={}", id, userId);
        orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id") Long userId) {
        log.info("POST /api/orders/{}/pay - userId={}", id, userId);
        OrderResponse response = orderService.payOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order paid successfully"));
    }
}
