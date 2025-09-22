package com.jmunoz.webfluxpatterns.sec01.controller;

import com.jmunoz.webfluxpatterns.sec01.dto.ProductAggregate;
import com.jmunoz.webfluxpatterns.sec01.service.ProductAggregatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sec01")
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
