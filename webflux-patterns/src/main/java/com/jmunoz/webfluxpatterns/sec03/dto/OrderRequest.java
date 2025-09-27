package com.jmunoz.webfluxpatterns.sec03.dto;

public record OrderRequest(Integer userId,
                           Integer productId,
                           Integer quantity) {
}
