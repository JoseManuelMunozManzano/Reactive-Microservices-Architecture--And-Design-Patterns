package com.jmunoz.webfluxpatterns.sec02.service;

import com.jmunoz.webfluxpatterns.sec02.client.DeltaClient;
import com.jmunoz.webfluxpatterns.sec02.client.FrontierClient;
import com.jmunoz.webfluxpatterns.sec02.client.JetBlueClient;
import com.jmunoz.webfluxpatterns.sec02.dto.FlightResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class FlightSearchService {

    private final DeltaClient deltaClient;
    private final FrontierClient frontierClient;
    private final JetBlueClient jetBlueClient;

    public FlightSearchService(DeltaClient deltaClient, FrontierClient frontierClient, JetBlueClient jetBlueClient) {
        this.deltaClient = deltaClient;
        this.frontierClient = frontierClient;
        this.jetBlueClient = jetBlueClient;
    }

    public Flux<FlightResult> getFlights(String from, String to) {
        // Todos estos publishers emiten valores en streaming, por eso usamos merge(), para combinar sus respuestas.
        return Flux.merge(
            this.deltaClient.getFlights(from, to),
            this.frontierClient.getFlights(from, to),
            this.jetBlueClient.getFlights(from, to)
        )
                .take(Duration.ofSeconds(3));
    }
}
