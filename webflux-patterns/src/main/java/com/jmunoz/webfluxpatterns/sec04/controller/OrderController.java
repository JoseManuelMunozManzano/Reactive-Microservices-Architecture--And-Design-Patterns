package com.jmunoz.webfluxpatterns.sec04.controller;

import com.jmunoz.webfluxpatterns.sec04.dto.OrderRequest;
import com.jmunoz.webfluxpatterns.sec04.dto.OrderResponse;
import com.jmunoz.webfluxpatterns.sec04.service.OrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sec04")
public class OrderController {

    private final OrchestratorService service;

    public OrderController(OrchestratorService service) {
        this.service = service;
    }

    @PostMapping("order")
    public Mono<ResponseEntity<OrderResponse>> placeOrder(@RequestBody Mono<OrderRequest> mono) {
        return this.service.placeOrder(mono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
