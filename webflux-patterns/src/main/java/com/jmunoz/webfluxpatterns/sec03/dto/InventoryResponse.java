package com.jmunoz.webfluxpatterns.sec03.dto;

public record InventoryResponse(Integer productId,
                                Integer quantity,
                                Integer remainingQuantity,
                                Status status) {
}
