package com.jmunoz.webfluxpatterns.sec01.dto;

import java.util.List;

// Esta es la información que el cliente (el navegador) necesita.
public record ProductAggregate(Integer id,
                               String category,
                               String description,
                               Price price,
                               List<Review> reviews
                                ) {
}
