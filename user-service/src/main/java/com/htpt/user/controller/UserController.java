package com.htpt.user.controller;

import com.htpt.common.dto.ApiResponse;
import com.htpt.user.dto.*;
import com.htpt.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - username={}", request.getUsername());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - username={}", request.getUsername());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("GET /api/users/me - userId={}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/users/{id:\\d+}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("PUT /api/users/me - userId={}", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated"));
    }

    @PostMapping("/api/users/{id}/deduct")
    public ResponseEntity<ApiResponse<Void>> deductBalance(
            @PathVariable Long id, 
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!"htpt-internal-secret-2024".equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("POST /api/users/{}/deduct", id);
        userService.deductBalance(id, body.get("amount"));
        return ResponseEntity.ok(ApiResponse.success(null, "Balance deducted"));
    }

    @PostMapping("/api/users/{id}/refund")
    public ResponseEntity<ApiResponse<Void>> refundBalance(
            @PathVariable Long id, 
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        if (!"htpt-internal-secret-2024".equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("POST /api/users/{}/refund", id);
        userService.refundBalance(id, body.get("amount"));
        return ResponseEntity.ok(ApiResponse.success(null, "Balance refunded"));
    }
}
