package com.jmunoz.webfluxpatterns.sec03.dto;

import java.util.UUID;

public record OrderResponse(Integer userId,
                            Integer product,
                            UUID orderId,
                            Status status,
                            Address shippingAddress,
                            String expectedDelivery) {
}
