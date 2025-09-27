package com.jmunoz.webfluxpatterns.sec03.client;

import com.jmunoz.webfluxpatterns.sec03.dto.PaymentRequest;
import com.jmunoz.webfluxpatterns.sec03.dto.PaymentResponse;
import com.jmunoz.webfluxpatterns.sec03.dto.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserClient {

    private static final String DEDUCT = "deduct";
    private static final String REFUND = "refund";
    private final WebClient client;

    public UserClient(@Value("${sec03.user.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<PaymentResponse> deduct(PaymentRequest request) {
        return this.callUserService(DEDUCT, request);
    }

    public Mono<PaymentResponse> refund(PaymentRequest request) {
        return this.callUserService(REFUND, request);
    }

    private Mono<PaymentResponse> callUserService(String endpoint, PaymentRequest request) {
        return this.client.post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                // Si falla convertimos a PaymentResponse con Status.FAILED
                .onErrorReturn(this.buildErrorResponse(request));
    }

    // Si falla convertimos a PaymentResponse con Status.FAILED
    private PaymentResponse buildErrorResponse(PaymentRequest request) {
        return new PaymentResponse(request.userId(),
                null,
                request.amount(),
                Status.FAILED
        );
    }
}
