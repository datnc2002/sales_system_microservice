package com.htpt.product.service;

import com.htpt.common.exception.BadRequestException;
import com.htpt.common.exception.ResourceNotFoundException;
import com.htpt.product.dto.*;
import com.htpt.product.model.Category;
import com.htpt.product.model.Product;
import com.htpt.product.repository.CategoryRepository;
import com.htpt.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching products page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findByActiveTrue(pageable)
                .map(ProductResponse::fromEntity);
    }

    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product created: id={}", product.getId());
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        log.info("Updating product id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product updated: id={}", product.getId());
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(String name, String description) {
        log.info("Creating category: {}", name);
        if (categoryRepository.existsByName(name)) {
            throw new BadRequestException("Category with name '" + name + "' already exists");
        }
        Category category = Category.builder().name(name).description(description).build();
        category = categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }
}
