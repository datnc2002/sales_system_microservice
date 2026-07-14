package com.htpt.inventory.service;

import com.htpt.common.exception.BadRequestException;
import com.htpt.common.exception.ResourceNotFoundException;
import com.htpt.common.event.InventoryEvent;
import com.htpt.common.event.OrderEvent;
import com.htpt.common.constants.KafkaConstants;
import com.htpt.inventory.model.Inventory;
import com.htpt.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public Inventory getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
    }

    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public Inventory updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for productId={}, quantity={}", productId, quantity);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(Inventory.builder().productId(productId).quantity(0).reserved(0).build());

        int oldStock = inventory.getQuantity();
        inventory.setQuantity(quantity);
        inventory = inventoryRepository.save(inventory);

        // Publish inventory updated event
        InventoryEvent event = InventoryEvent.inventoryUpdated(
                productId, "Product#" + productId, oldStock, quantity, "Manual update");
        kafkaTemplate.send(KafkaConstants.INVENTORY_EVENTS_TOPIC, productId.toString(), event);

        return inventory;
    }

    @Transactional
    public void deductStock(OrderEvent orderEvent) {
        log.info("Deducting stock for orderId={}", orderEvent.getOrderId());
        for (OrderEvent.OrderItemEvent item : orderEvent.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElse(Inventory.builder().productId(item.getProductId()).quantity(0).reserved(0).build());

            int oldStock = inventory.getQuantity();
            int newStock = oldStock - item.getQuantity();

            if (newStock < 0) {
                log.warn("Insufficient stock for productId={}: available={}, requested={}",
                        item.getProductId(), oldStock, item.getQuantity());
                newStock = 0;
            }

            inventory.setQuantity(newStock);
            inventoryRepository.save(inventory);
            log.info("Stock deducted: productId={}, {} -> {}", item.getProductId(), oldStock, newStock);

            // Publish event for low stock notification
            if (newStock <= 5) {
                InventoryEvent event = InventoryEvent.inventoryUpdated(
                        item.getProductId(), item.getProductName(), oldStock, newStock,
                        "Order#" + orderEvent.getOrderId() + " - Low stock warning");
                kafkaTemplate.send(KafkaConstants.INVENTORY_EVENTS_TOPIC, item.getProductId().toString(), event);
            }
        }
    }

    @Transactional
    public void restoreStock(OrderEvent orderEvent) {
        log.info("Restoring stock for cancelled orderId={}", orderEvent.getOrderId());
        for (OrderEvent.OrderItemEvent item : orderEvent.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElse(Inventory.builder().productId(item.getProductId()).quantity(0).reserved(0).build());

            int oldStock = inventory.getQuantity();
            int newStock = oldStock + item.getQuantity();
            inventory.setQuantity(newStock);
            inventoryRepository.save(inventory);
            log.info("Stock restored: productId={}, {} -> {}", item.getProductId(), oldStock, newStock);
        }
    }
}
