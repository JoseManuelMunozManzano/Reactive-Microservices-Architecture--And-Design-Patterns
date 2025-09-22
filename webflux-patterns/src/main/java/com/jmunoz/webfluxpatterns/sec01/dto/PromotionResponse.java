package com.jmunoz.webfluxpatterns.sec01.dto;

import java.time.LocalDate;

// Sé que son estos campos porque he visto la respuesta que da Swagger al servicio externo.
public record PromotionResponse(Integer id,
                                String type,
                                Double discount,
                                LocalDate endDate) {
}
