package com.jmunoz.webfluxpatterns.sec03.dto;

import java.util.UUID;

public record ShippingRequest(Integer quantity,
                              Integer userId,
                              UUID orderId) {
}
