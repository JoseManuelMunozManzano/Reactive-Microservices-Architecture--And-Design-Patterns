package com.jmunoz.webfluxpatterns.sec04.dto;

import java.util.UUID;

public record InventoryRequest(UUID paymentId,
                               Integer productId,
                               Integer quantity) {
}
