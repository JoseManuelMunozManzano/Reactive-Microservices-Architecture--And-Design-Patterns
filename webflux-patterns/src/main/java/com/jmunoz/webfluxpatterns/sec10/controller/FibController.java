package com.jmunoz.webfluxpatterns.sec10.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

// CPU intensive
@RestController
@RequestMapping("sec10")
public class FibController {

    // Tengo 8 cores (1 core = 1 cpu) en mi ordenador, pero lo limito a 6.
    // Bulkhead indica el número de llamadas en paralelo que puedo realizar a la vez.
    // Esto podría ser un bean también.
    private final Scheduler scheduler = Schedulers.newParallel("fib", 6);

    // Secuencia de Fibonacci: 0, 1, 1, 2, 3, 5, 8, 13, 21, ...

    // CPU Intensive!!
    // Una forma de solucionar el problema sería usar la anotación @Bulkhead() de Resilience4j aquí,
    // pero no lo recomiendo porque Resilience4j es muy buena para Spring Mvc Y Spring WebFlux, pero
    // la configuración bloquearía el thread en algunos casos, lo que afectaría al rendimiento.
    //
    // Vamos a solucionarlo usando el mismo Reactor.
    // Ya sabemos que:
    // - Si usamos bloqueos IO intensivos, tenemos que usar Schedulers.boundedElastic()
    // - Si usamos computación intensiva, tenemos que usar Schedulers.parallel()
    //
    // Lo importante es identificar si esta API es de uso intensivo de CPU o uso intensivo de IO.
    // En este caso es de uso intensivo de CPU, así que usaremos Schedulers.parallel().
    // Usando el scheduler, nuestro event loop thread no quedará atascado.
    //
    // También podemos añadir el patrón Rate Limiter para limitar las llamadas a la API en una ventana de x segundos.
    // También podemos añadir el patrón Timeout para, si tarda mucho tiempo en responder, indicar un máximo tiempo
    //   de espera y, si se sobrepasa, devolver un fallback value.
    @GetMapping("fib/{input}")
    public Mono<ResponseEntity<Long>> fib(@PathVariable Long input) {
        return Mono.fromSupplier(() -> findFib(input))
                .subscribeOn(scheduler)
                .map(ResponseEntity::ok);
    }

    // Lo hacemos recursivo para que consuma más recursos de CPU.
    private Long findFib(Long input) {
        if (input < 2)
            return input;

        return findFib(input - 1) + findFib(input - 2);
    }
}
