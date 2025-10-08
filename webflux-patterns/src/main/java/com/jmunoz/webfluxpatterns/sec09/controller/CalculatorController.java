package com.jmunoz.webfluxpatterns.sec09.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sec09")
public class CalculatorController {

    // Hacemos este controller para demostrar como funciona Server Side Rate Limiter.
    // Tenemos que imaginar que es una tarea de cómputo intensivo, como procesamiento de imágenes.
    // CPU Intensive!!
    // Permitimos 5 request / 20 seconds
    //
    // Rate Limiter lo conseguimos usando la anotación de Resilience4j @RateLimiter, indicando
    // el nombre de la instance que podemos ver en application.yaml, y el fallback que se ejecutará.
    @GetMapping("calculator/{input}")
    @RateLimiter(name = "calculator-service", fallbackMethod = "fallback")
    public Mono<ResponseEntity<Integer>> doubleInput(@PathVariable Integer input) {
        return Mono.fromSupplier(() -> input * 2)
                .map(ResponseEntity::ok);
    }

    // Este fallback debe tener la misma firma (añadiendo la exception Details)
    public Mono<ResponseEntity<String>> fallback(Integer input, Throwable ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage()));
    }
}
