package com.jmunoz.webfluxpatterns.sec07.service;

import com.jmunoz.webfluxpatterns.sec07.client.ProductClient;
import com.jmunoz.webfluxpatterns.sec07.client.ReviewClient;
import com.jmunoz.webfluxpatterns.sec07.dto.Product;
import com.jmunoz.webfluxpatterns.sec07.dto.ProductAggregate;
import com.jmunoz.webfluxpatterns.sec07.dto.Review;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ProductAggregatorService {

    private final ProductClient productClient;
    private final ReviewClient reviewClient;

    public ProductAggregatorService(ProductClient productClient, ReviewClient reviewClient) {
        this.productClient = productClient;
        this.reviewClient = reviewClient;
    }

    // Es un mono porque solo enviamos 1 ProductAggregate.
    public Mono<ProductAggregate> aggregate(Integer id) {
        // zip() acepta varios publishers y los ejecuta en paralelo. Las respuestas se devuelven en una tupla.
        // O ejecuta to-do correctamente o nada.
        // Si algún publisher emite la señal empty, Mono.zip() emitirá la señal empty.
        // Si zip() recibe la señal empty gestiona la excepción.
        return Mono.zip(
                        this.productClient.getProduct(id),
                        this.reviewClient.getReviews(id)
                )
                .map(t -> toDto(t.getT1(), t.getT2()));
    }

    private ProductAggregate toDto(Product product, List<Review> reviews) {
        return new ProductAggregate(
                product.id(),
                product.category(),
                product.description(),
                reviews
        );
    }
}
