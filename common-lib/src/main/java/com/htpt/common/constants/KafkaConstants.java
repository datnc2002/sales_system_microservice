package com.htpt.common.constants;

public final class KafkaConstants {

    private KafkaConstants() {}

    // Topics
    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    // Consumer Groups
    public static final String INVENTORY_GROUP = "inventory-group";
    public static final String NOTIFICATION_ORDER_GROUP = "notification-order-group";
    public static final String NOTIFICATION_INVENTORY_GROUP = "notification-inventory-group";

    // Event Types
    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String ORDER_PAID = "ORDER_PAID";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String INVENTORY_UPDATED = "INVENTORY_UPDATED";
}
