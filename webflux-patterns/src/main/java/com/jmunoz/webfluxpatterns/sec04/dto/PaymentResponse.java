package com.jmunoz.webfluxpatterns.sec04.dto;

import java.util.UUID;

public record PaymentResponse(UUID paymentId,
                              Integer userId,
                              String name,
                              Integer balance,
                              Status status) {
}
