package org.springboot.smartdining.enums;

/** Tình trạng giao đơn mang về (TKW-4), suy ra từ đơn và các món, không lưu trong database. */
public enum DeliveryState {
    PREPARING,
    READY,
    DELIVERED,
    CANCELLED
}
