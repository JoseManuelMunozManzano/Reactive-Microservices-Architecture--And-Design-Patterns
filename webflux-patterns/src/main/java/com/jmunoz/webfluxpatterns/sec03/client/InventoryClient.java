package com.jmunoz.webfluxpatterns.sec03.client;

import com.jmunoz.webfluxpatterns.sec03.dto.InventoryRequest;
import com.jmunoz.webfluxpatterns.sec03.dto.InventoryResponse;
import com.jmunoz.webfluxpatterns.sec03.dto.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class InventoryClient {

    private static final String DEDUCT = "deduct";
    private static final String RESTORE = "restore";
    private final WebClient client;

    public InventoryClient(@Value("${sec03.inventory.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<InventoryResponse> deduct(InventoryRequest request) {
        return this.callInventoryService(DEDUCT, request);
    }

    public Mono<InventoryResponse> restore(InventoryRequest request) {
        return this.callInventoryService(RESTORE, request);
    }

    private Mono<InventoryResponse> callInventoryService(String endpoint, InventoryRequest request) {
        return this.client.post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(InventoryResponse.class)
                // Si falla convertimos a InventoryResponse con Status.FAILED
                .onErrorReturn(this.buildErrorResponse(request));
    }

    // Si falla convertimos a InventoryResponse con Status.FAILED
    private InventoryResponse buildErrorResponse(InventoryRequest request) {
        return new InventoryResponse(request.productId(),
                request.quantity(),
                null,
                Status.FAILED
        );
    }
}
