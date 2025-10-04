package com.jmunoz.webfluxpatterns.sec05.client;

import com.jmunoz.webfluxpatterns.sec05.dto.CarReservationRequest;
import com.jmunoz.webfluxpatterns.sec05.dto.CarReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CarClient {

    private final WebClient client;

    public CarClient(@Value("${sec05.car.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Flux<CarReservationResponse> reserve(Flux<CarReservationRequest> flux) {
        return this.client.post()
                // No hace falta uri porque ya viene en baseUrl.
                // Usamos body porque es un Publisher (usaríamos bodyValue si fuera un objeto).
                .body(flux, CarReservationRequest.class)
                .retrieve()
                .bodyToFlux(CarReservationResponse.class)
                .onErrorResume(ex -> Mono.empty());
    }
}
