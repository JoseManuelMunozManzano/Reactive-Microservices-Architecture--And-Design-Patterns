package com.jmunoz.webfluxpatterns.sec05.client;

import com.jmunoz.webfluxpatterns.sec05.dto.RoomReservationRequest;
import com.jmunoz.webfluxpatterns.sec05.dto.RoomReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoomClient {

    private final WebClient client;

    public RoomClient(@Value("${sec05.room.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Flux<RoomReservationResponse> reserve(Flux<RoomReservationRequest> flux) {
        return this.client.post()
                // No hace falta uri porque ya viene en baseUrl.
                // Usamos body porque es un Publisher (usaríamos bodyValue si fuera un objeto).
                .body(flux, RoomReservationRequest.class)
                .retrieve()
                .bodyToFlux(RoomReservationResponse.class)
                .onErrorResume(ex -> Mono.empty());
    }
}
