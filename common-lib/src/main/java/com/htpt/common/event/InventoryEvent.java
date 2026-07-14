package com.htpt.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryEvent extends BaseEvent {

    private Long productId;
    private String productName;
    private Integer oldStock;
    private Integer newStock;
    private String reason;

    public static InventoryEvent inventoryUpdated(Long productId, String productName,
                                                   Integer oldStock, Integer newStock, String reason) {
        InventoryEvent event = InventoryEvent.builder()
                .productId(productId)
                .productName(productName)
                .oldStock(oldStock)
                .newStock(newStock)
                .reason(reason)
                .build();
        event.initEvent("INVENTORY_UPDATED");
        return event;
    }
}
