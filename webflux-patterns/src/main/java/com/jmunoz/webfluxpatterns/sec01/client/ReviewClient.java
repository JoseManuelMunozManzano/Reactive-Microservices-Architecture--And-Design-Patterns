package com.jmunoz.webfluxpatterns.sec01.client;

import com.jmunoz.webfluxpatterns.sec01.dto.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class ReviewClient {
    private final WebClient client;

    public ReviewClient(@Value("${sec01.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<Review>> getReviews(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                .bodyToFlux(Review.class)
                // No es bloqueante, pero espera hasta que el provider emita la señal onComplete para dar los items.
                .collectList()
                // Para hacerlo más resiliente y poder construir el producto, si ocurre cualquier tipo de error,
                // supondremos que no hay reviews. Como es una lista vacía, funciona con zip().
                .onErrorReturn(Collections.emptyList());
    }
}
