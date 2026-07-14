package com.htpt.order.client;

import com.htpt.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ApiResponse<ProductDto> getProduct(@PathVariable("id") Long id);

    record ProductDto(Long id, String name, String description, BigDecimal price,
                      Long categoryId, String categoryName, String imageUrl, Boolean active) {}
}
