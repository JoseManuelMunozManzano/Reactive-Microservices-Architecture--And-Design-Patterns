package com.jmunoz.webfluxpatterns.sec05.controller;

import com.jmunoz.webfluxpatterns.sec05.dto.ReservationItemRequest;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationResponse;
import com.jmunoz.webfluxpatterns.sec05.service.ReservationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sec05")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("reserve")
    public Mono<ReservationResponse> reserve(@RequestBody Flux<ReservationItemRequest> flux) {
        return this.service.reserve(flux);
    }
}
