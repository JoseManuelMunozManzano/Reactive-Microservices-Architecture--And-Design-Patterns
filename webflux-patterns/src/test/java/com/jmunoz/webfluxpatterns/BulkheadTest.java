package com.jmunoz.webfluxpatterns;

import com.jmunoz.webfluxpatterns.sec10.dto.ProductAggregate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BulkheadTest {

    private WebClient client;

    // Añadir la anotación @TestInstance(TestInstance.Lifecycle.PER_CLASS) para que esto no falle.
    @BeforeAll
    public void setClient() {
        this.client = WebClient.builder()
                .baseUrl("http://localhost:8080/sec10/")
                .build();
    }

    // Llamamos en paralelo tanto a las peticiones de fibonacci como a las de obtención de información de producto.
    @Test
    public void concurrentUsersTest() {
        StepVerifier.create(Flux.merge(fibRequests(), productRequests()))
                .verifyComplete();
    }

    // Hará 4 peticiones en paralelo para calcular el fibonacci de 47.
    // Uso intensivo de CPU.
    private Mono<Void> fibRequests() {
        // Probar primero con 2 peticiones y luego con 40. Esto es porque la prueba depende del número de núcleos de nuestra CPU.
        return Flux.range(1, 40)
                .flatMap(i -> this.client.get().uri("fib/47").retrieve().bodyToMono(Long.class))
                .doOnNext(this::print)
                .then();
    }

    // Hará 4 peticiones en paralelo para obtener información de un producto.
    // Retrasamos su comienzo porque queremos que empiecen primero las peticiones del número de fibonacci.
    private Mono<Void> productRequests() {
        // Probar primero con 2 peticiones y luego con 40. Esto es porque la prueba depende del número de núcleos de nuestra CPU.
        return Mono.delay(Duration.ofMillis(100))
                .thenMany(Flux.range(1, 40))
                .flatMap(i -> this.client.get().uri("product/1").retrieve().bodyToMono(ProductAggregate.class))
                .map(ProductAggregate::category)
                .doOnNext(this::print)
                .then();
    }

    private void print(Object o) {
        System.out.println(LocalDateTime.now() + " : " + o);
    }
}
