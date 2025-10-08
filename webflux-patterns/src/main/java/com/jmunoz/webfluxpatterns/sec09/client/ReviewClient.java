package com.jmunoz.webfluxpatterns.sec09.client;

import com.jmunoz.webfluxpatterns.sec09.dto.Review;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class ReviewClient {
    private final WebClient client;

    public ReviewClient(@Value("${sec09.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Añadimos Client Side Rate Limiter.
    // Rate Limiter lo conseguimos usando la anotación de Resilience4j @RateLimiter, indicando
    // el nombre de la instance que podemos ver en application.yaml, y el fallback que se ejecutará.
    @RateLimiter(name = "review-service", fallbackMethod = "fallback")
    public Mono<List<Review>> getReviews(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                // Para que no ejecute el retry pattern si el error es de tipo 4XX.
                // Cambiamos el mensaje por una señal empty.
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToFlux(Review.class)
                // No es bloqueante, pero espera hasta que el provider emita la señal onComplete para dar los items.
                .collectList();
                // La respuesta de la petición se podría cachear (ver documentación más abajo)
                // .doOnNext(list -> // put in cache)
    }

    // Este fallback debe tener la misma firma (añadiendo la exception Details)
    // Este es un ejemplo en el que devolvemos una lista vacía, pero lo suyo es
    // cachear la respuesta de una petición exitosa y, cuando se ejecute este fallback,
    // devolver la información cacheada.
    public Mono<List<Review>> fallback(Integer id, Throwable ex) {
        // Si la información estuviera cacheada, se podría hacer esto
        // return Mono.fromSupplier(() -> // read from cache id)
        return Mono.just(Collections.emptyList());
    }
}
