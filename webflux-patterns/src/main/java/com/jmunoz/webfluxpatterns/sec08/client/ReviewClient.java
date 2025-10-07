package com.jmunoz.webfluxpatterns.sec08.client;

import com.jmunoz.webfluxpatterns.sec08.dto.Review;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    public ReviewClient(@Value("${sec08.review.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Devolvemos bien una lista vacía o una lista de reviews.
    // Implementamos el patrón Circuit Breaker usando esta anotación, que espera dos propiedades:
    //    - name: Los indicamos en application.yaml
    //    - fallbackMethod: Lo que se ejecuta cuando estamos en estado OPEN.
    // ¡IMPLEMENTAMOS 3 PATRONES AQUÍ!
    @CircuitBreaker(name = "review-service", fallbackMethod = "fallbackReview")
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
                // Pero esta es una excepción de TimeOut que no estaba añadida en application.yaml,
                // ya que solo teníamos WebClientResponseException, así que Circuit Breaker la ignora.
                // Para que no la ignore, añadimos ese tipo de excepción también en application.yaml.
                .timeout(Duration.ofMillis(300));
                // Para hacerlo más resiliente y poder construir el producto, si ocurre cualquier tipo de error,
                // supondremos que no hay reviews. Como es una lista vacía, funciona con zip().
                // PERO CircuitBreaker tiene que ver la excepción!! Si no, supondrá que siempre funciona bien.
                // Así que lo tenemos que quitar:
                //
                // .onErrorReturn(Collections.emptyList());
    }

    // Este fallback method no puede ser privado porque lo invoca Spring AOP.
    // Tiene que devolver el mismo objeto que el méto-do que tiene la anotación, y tener los mismos parámetros,
    // al que se le suma la excepción, que también hemos indicado en application.yaml.
    public Mono<List<Review>> fallbackReview(Integer id, Throwable ex) {
        System.out.println("fallback reviews called : " + ex.getMessage());
        return Mono.just(Collections.emptyList());
    }
}
