package com.jmunoz.webfluxpatterns.sec03.dto;

import java.util.UUID;

public record InventoryRequest(UUID orderId,
                               Integer productId,
                               Integer quantity) {
}
