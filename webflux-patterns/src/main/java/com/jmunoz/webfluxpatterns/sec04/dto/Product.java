package com.jmunoz.webfluxpatterns.sec04.dto;

public record Product(Integer id,
                      String category,
                      String description,
                      Integer price) {
}
