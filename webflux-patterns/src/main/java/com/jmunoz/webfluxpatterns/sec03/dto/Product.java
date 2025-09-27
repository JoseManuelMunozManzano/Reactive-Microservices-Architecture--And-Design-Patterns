package com.jmunoz.webfluxpatterns.sec03.dto;

public record Product(Integer id,
                      String category,
                      String description,
                      Integer price) {
}
