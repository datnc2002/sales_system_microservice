package com.htpt.inventory.controller;

import com.htpt.common.dto.ApiResponse;
import com.htpt.inventory.model.Inventory;
import com.htpt.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Inventory>>> getAll() {
        log.info("GET /api/inventory");
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAll()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> getByProductId(@PathVariable Long productId) {
        log.info("GET /api/inventory/{}", productId);
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByProductId(productId)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> updateStock(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> body) {
        log.info("PUT /api/inventory/{}", productId);
        Inventory inventory = inventoryService.updateStock(productId, body.get("quantity"));
        return ResponseEntity.ok(ApiResponse.success(inventory, "Stock updated"));
    }
}
