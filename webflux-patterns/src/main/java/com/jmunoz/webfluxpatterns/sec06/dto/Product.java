package com.jmunoz.webfluxpatterns.sec06.dto;

// Sé que son estos campos porque he visto la respuesta que da Swagger al servicio externo.
public record Product(Integer id,
                      String category,
                      String description,
                      Integer price) {
}
