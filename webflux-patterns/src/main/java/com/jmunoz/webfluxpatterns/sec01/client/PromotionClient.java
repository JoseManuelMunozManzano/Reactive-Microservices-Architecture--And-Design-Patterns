package com.jmunoz.webfluxpatterns.sec01.client;

import com.jmunoz.webfluxpatterns.sec01.dto.PromotionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class PromotionClient {

    private final PromotionResponse noPromotion = new PromotionResponse(-1, "no promotion", 0.0, LocalDate.now());
    private final WebClient client;

    public PromotionClient(@Value("${sec01.promotion.service}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<PromotionResponse> getPromotion(Integer id) {
        return this.client.get()
                .uri("{id}", id)
                .retrieve()
                .bodyToMono(PromotionResponse.class)
                // Para hacerlo más resiliente y poder construir el producto, si ocurre cualquier tipo de error,
                // supondremos que no hay promociones.
                // Pero esto no funciona porque si emitimos un empty, zip() ya emite también empty.
                //
                //.onErrorResume(ex -> Mono.empty());
                //
                // Lo que hacemos es emitir un objeto noPromotion (objeto por defecto)
                .onErrorReturn(noPromotion);
    }
}
