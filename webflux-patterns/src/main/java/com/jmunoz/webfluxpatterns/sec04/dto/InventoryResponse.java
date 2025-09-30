package com.jmunoz.webfluxpatterns.sec04.dto;

import java.util.UUID;

public record InventoryResponse(UUID inventoryId,
                                Integer productId,
                                Integer quantity,
                                Integer remainingQuantity,
                                Status status) {
}
