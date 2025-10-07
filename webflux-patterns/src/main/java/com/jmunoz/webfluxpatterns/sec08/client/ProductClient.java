package com.jmunoz.webfluxpatterns.sec08.client;

import com.jmunoz.webfluxpatterns.sec08.dto.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProductClient {

    private final WebClient client;

    public ProductClient(@Value("${sec08.product.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Devolvemos bien 404 o un Product.
    public Mono<Product> getProduct(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                .bodyToMono(Product.class)
                // Si se emite la señal de error, devolvemos empty, traducido a 404.
                .onErrorResume(ex -> Mono.empty());
    }
}
