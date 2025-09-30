package com.jmunoz.webfluxpatterns.sec04.dto;

import java.util.UUID;

public record ShippingResponse(UUID shippingId,
                               Integer quantity,
                               Status status,
                               String expectedDelivery,
                               Address address) {
}
