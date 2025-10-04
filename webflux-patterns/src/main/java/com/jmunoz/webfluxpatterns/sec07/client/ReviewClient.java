package com.jmunoz.webfluxpatterns.sec07.client;

import com.jmunoz.webfluxpatterns.sec07.dto.Review;
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

    public ReviewClient(@Value("${sec07.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Devolvemos bien una lista vacía o una lista de reviews.
    public Mono<List<Review>> getReviews(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                // Para que no ejecute el retry pattern si el error es de tipo 4XX.
                // Cambiamos el mensaje por una señal empty.
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToFlux(Review.class)
                // No es bloqueante, pero espera hasta que el provider emita la señal onComplete para dar los items.
                .collectList()
                // Aplicamos el patrón Retry, indicando cuantas veces vamos a reintentar.
                // Esto es perfecto para errores tipo 5XX.
                // Cuando queramos esperar un poco antes de mandar una nueva petición, usaremos retryWhen().
                // Usando retry() la petición se hace en el momento en que sabemos que hay un error que no es del tipo 4XX.
                .retry(5)
                // Como cada reintento lleva su tiempo, si encima el servicio externo es lento, esto puede ser fatal.
                // Por tanto, siempre que se incluya el patrón Retry, debería incluirse el patrón Timeout.
                // Con esto, reintentamos 5 veces, pero hasta una espera máxima de 300 ms. Pasado este tiempo,
                // si sigue fallando, devolvemos la señal de error.
                .timeout(Duration.ofMillis(300))
                // Para hacerlo más resiliente y poder construir el producto, si ocurre cualquier tipo de error,
                // supondremos que no hay reviews. Como es una lista vacía, funciona con zip().
                .onErrorReturn(Collections.emptyList());
    }
}
