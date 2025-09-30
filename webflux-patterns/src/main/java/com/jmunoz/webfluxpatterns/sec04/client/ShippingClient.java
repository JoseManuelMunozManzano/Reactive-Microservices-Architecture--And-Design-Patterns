package com.jmunoz.webfluxpatterns.sec04.client;

import com.jmunoz.webfluxpatterns.sec04.dto.ShippingRequest;
import com.jmunoz.webfluxpatterns.sec04.dto.ShippingResponse;
import com.jmunoz.webfluxpatterns.sec04.dto.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ShippingClient {

    private static final String SCHEDULE = "schedule";
    private static final String CANCEL = "cancel";
    private final WebClient client;

    public ShippingClient(@Value("${sec04.shipping.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<ShippingResponse> schedule(ShippingRequest request) {
        return this.callShippingService(SCHEDULE, request);
    }

    public Mono<ShippingResponse> cancel(ShippingRequest request) {
        return this.callShippingService(CANCEL, request);
    }

    private Mono<ShippingResponse> callShippingService(String endpoint, ShippingRequest request) {
        return this.client.post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ShippingResponse.class)
                // Si falla convertimos a ShippingResponse con Status.FAILED
                .onErrorReturn(this.buildErrorResponse(request));
    }

    // Si falla convertimos a ShippingResponse con Status.FAILED
    private ShippingResponse buildErrorResponse(ShippingRequest request) {
        return new ShippingResponse(
                null,
                request.quantity(),
                Status.FAILED,
                null,
                null
        );
    }
}
