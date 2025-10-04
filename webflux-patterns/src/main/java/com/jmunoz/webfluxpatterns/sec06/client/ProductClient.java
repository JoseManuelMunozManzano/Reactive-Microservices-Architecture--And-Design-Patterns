package com.jmunoz.webfluxpatterns.sec06.client;

import com.jmunoz.webfluxpatterns.sec06.dto.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ProductClient {

    private final WebClient client;

    public ProductClient(@Value("${sec06.product.service}") String baseUrl) {
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
                // Patrón Timeout: Espero la respuesta en medio segundo.
                // Si no, emito la señal de error.
                // Como parte de su firma, también podemos llamar a otro Publisher como fallback.
                // Pueden ser valores cacheados, tipo .timeout(Duration.ofMillis(500), cacheService.getProduct())
                .timeout(Duration.ofMillis(500))
                // Si se emite la señal de error, devolvemos empty, traducido a 404.
                .onErrorResume(ex -> Mono.empty());
    }
}
