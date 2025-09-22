package com.jmunoz.webfluxpatterns.sec02.client;

import com.jmunoz.webfluxpatterns.sec02.dto.FlightResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class JetBlueClient {

    private static final String JETBLUE = "JETBLUE";
    private final WebClient client;

    public JetBlueClient(@Value("${sec02.jetblue.service}") String baseUrl) {
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
                // Para cada registro normalizamos el resultado.
                .map(fr -> this.normalizeResponse(fr, from, to))
                // Obtenemos todas las respuestas posibles.
                // Si hay una señal de error, paramos y reemplazamos con una señal de completado.
                .onErrorResume(ex -> Mono.empty());
    }

    // Esta información no la tenemos.
    private FlightResult normalizeResponse(FlightResult result, String from, String to) {
        return new FlightResult(JETBLUE, from, to, result.price(), result.date());
    }
}
