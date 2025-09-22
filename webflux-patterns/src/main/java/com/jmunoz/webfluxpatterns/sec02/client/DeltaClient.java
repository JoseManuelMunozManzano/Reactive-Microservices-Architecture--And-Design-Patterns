package com.jmunoz.webfluxpatterns.sec02.client;

import com.jmunoz.webfluxpatterns.sec02.dto.FlightResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// En la vida real, se podría crear una interface y que cada servicio lo implementara.
// Esto es porque van a recuperar la misma información.
// Pero vamos a hacerlo simple.
@Service
public class DeltaClient {

    private final WebClient client;

    public DeltaClient(@Value("${sec02.delta.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Flux<FlightResult> getFlights(String from, String to) {
        return this.client
                .get()
                .uri("{from}/{to}", from, to)
                .retrieve()
                .bodyToFlux(FlightResult.class)
                // Obtenemos todas las respuestas posibles.
                // Si hay una señal de error, paramos y reemplazamos con una señal de completado.
                .onErrorResume(ex -> Mono.empty());
    }
}
