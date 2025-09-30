package com.jmunoz.webfluxpatterns.sec04.dto;

public record OrderRequest(Integer userId,
                           Integer productId,
                           Integer quantity) {
}
