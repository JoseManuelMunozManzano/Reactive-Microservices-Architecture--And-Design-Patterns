package com.jmunoz.webfluxpatterns.sec10.client;

import com.jmunoz.webfluxpatterns.sec10.dto.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class ReviewClient {
    private final WebClient client;

    public ReviewClient(@Value("${sec10.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<List<Review>> getReviews(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                // Cambiamos el mensaje por una señal empty.
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToFlux(Review.class)
                // No es bloqueante, pero espera hasta que el provider emita la señal onComplete para dar los items.
                .collectList();
    }
}
