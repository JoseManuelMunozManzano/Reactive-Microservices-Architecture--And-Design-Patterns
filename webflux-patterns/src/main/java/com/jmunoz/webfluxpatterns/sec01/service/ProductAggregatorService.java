package com.jmunoz.webfluxpatterns.sec01.service;

import com.jmunoz.webfluxpatterns.sec01.client.ProductClient;
import com.jmunoz.webfluxpatterns.sec01.client.PromotionClient;
import com.jmunoz.webfluxpatterns.sec01.client.ReviewClient;
import com.jmunoz.webfluxpatterns.sec01.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ProductAggregatorService {

    private final ProductClient productClient;
    private final PromotionClient promotionClient;
    private final ReviewClient reviewClient;

    public ProductAggregatorService(ProductClient productClient, PromotionClient promotionClient, ReviewClient reviewClient) {
        this.productClient = productClient;
        this.promotionClient = promotionClient;
        this.reviewClient = reviewClient;
    }

    // Es un mono porque solo enviamos 1 ProductAggregate.
    public Mono<ProductAggregate> aggregate(Integer id) {
        // zip() acepta varios publishers y los ejecuta en paralelo. Las respuestas se devuelven en una tupla.
        // O ejecuta to-do correctamente o nada.
        // Si algún publisher emite la señal empty, Mono.zip() emitirá la señal empty.
        // Si zip() recibe la señal empty (solo posible desde ProductClient) gestiona la excepción.
        return Mono.zip(
                        this.productClient.getProduct(id),
                        this.promotionClient.getPromotion(id),
                        this.reviewClient.getReviews(id)
                )
                .map(t -> toDto(t.getT1(), t.getT2(), t.getT3()));
    }

    private ProductAggregate toDto(ProductResponse product, PromotionResponse promotion, List<Review> reviews) {
        var amountSaved = product.price() * promotion.discount() / 100;
        var discountedPrice = product.price() - amountSaved;
        var price = new Price(
                product.price(),
                promotion.discount(),
                discountedPrice,
                amountSaved,
                promotion.endDate()
        );

        return new ProductAggregate(
                product.id(),
                product.category(),
                product.description(),
                price,
                reviews
        );
    }
}
