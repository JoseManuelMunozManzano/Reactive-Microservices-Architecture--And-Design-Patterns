package com.jmunoz.webfluxpatterns.sec06.client;

import com.jmunoz.webfluxpatterns.sec06.dto.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class ReviewClient {
    private final WebClient client;

    public ReviewClient(@Value("${sec06.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Devolvemos bien una lista vacía o una lista de reviews.
    public Mono<List<Review>> getReviews(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                .bodyToFlux(Review.class)
                // No es bloqueante, pero espera hasta que el provider emita la señal onComplete para dar los items.
                .collectList()
                // Patrón Timeout: Espero la respuesta en medio segundo.
                // Si no, emito la señal de error.
                // Como parte de su firma, también podemos llamar a otro Publisher como fallback.
                // Pueden ser valores cacheados, tipo .timeout(Duration.ofMillis(500), cacheService.getReviews())
                .timeout(Duration.ofMillis(500))
                // Para hacerlo más resiliente y poder construir el producto, si ocurre cualquier tipo de error,
                // incluido el de que sobrepasamos el tiempo de Timeout indicado,
                // supondremos que no hay reviews. Como es una lista vacía, funciona con zip().
                .onErrorReturn(Collections.emptyList());
    }
}
