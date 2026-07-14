package com.htpt.order.client;

import com.htpt.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url:http://htpt-user-service:8081}")
public interface UserClient {

    @PostMapping("/api/users/{id}/deduct")
    ApiResponse<Void> deductBalance(
            @PathVariable("id") Long id, 
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader("X-Internal-Secret") String secret);

    @PostMapping("/api/users/{id}/refund")
    ApiResponse<Void> refundBalance(
            @PathVariable("id") Long id, 
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader("X-Internal-Secret") String secret);
}
