package com.jmunoz.webfluxpatterns.sec10.dto;

// Sé que son estos campos porque he visto la respuesta que da Swagger al servicio externo.
public record Review(Integer id,
                     String user,
                     Integer rating,
                     String comment) {
}
