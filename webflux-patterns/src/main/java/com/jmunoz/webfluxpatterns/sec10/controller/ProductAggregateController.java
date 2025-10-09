package com.jmunoz.webfluxpatterns.sec10.controller;

import com.jmunoz.webfluxpatterns.sec10.dto.ProductAggregate;
import com.jmunoz.webfluxpatterns.sec10.service.ProductAggregatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

// IO Calls, llamadas de red.
@RestController
@RequestMapping("sec10")
public class ProductAggregateController {
    private final ProductAggregatorService service;

    public ProductAggregateController(ProductAggregatorService service) {
        this.service = service;
    }

    @GetMapping("product/{id}")
    public Mono<ResponseEntity<ProductAggregate>> getProductAggregate(@PathVariable Integer id) {
        return this.service.aggregate(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }
}
