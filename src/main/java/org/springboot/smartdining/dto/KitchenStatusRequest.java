package org.springboot.smartdining.dto;

import jakarta.validation.constraints.NotNull;
import org.springboot.smartdining.enums.OrderItemStatus;

public record KitchenStatusRequest(@NotNull OrderItemStatus status) {
}
