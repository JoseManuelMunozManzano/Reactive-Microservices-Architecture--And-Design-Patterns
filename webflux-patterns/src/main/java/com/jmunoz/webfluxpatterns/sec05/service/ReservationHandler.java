package com.jmunoz.webfluxpatterns.sec05.service;

import com.jmunoz.webfluxpatterns.sec05.dto.ReservationItemRequest;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationItemResponse;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationType;
import reactor.core.publisher.Flux;

public abstract class ReservationHandler {

    protected abstract ReservationType getType();
    protected abstract Flux<ReservationItemResponse> reserve(Flux<ReservationItemRequest> flux);
}
