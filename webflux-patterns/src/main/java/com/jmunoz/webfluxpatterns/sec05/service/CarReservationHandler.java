package com.jmunoz.webfluxpatterns.sec05.service;

import com.jmunoz.webfluxpatterns.sec05.client.CarClient;
import com.jmunoz.webfluxpatterns.sec05.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CarReservationHandler extends ReservationHandler {

    private final CarClient client;

    public CarReservationHandler(CarClient client) {
        this.client = client;
    }

    @Override
    protected ReservationType getType() {
        return ReservationType.CAR;
    }

    @Override
    protected Flux<ReservationItemResponse> reserve(Flux<ReservationItemRequest> flux) {
        return flux.map(this::toCarRequest)
                // map transforma un objeto en otro objeto.
                // transform transforma un flux en otro flux.
                .transform(this.client::reserve)
                .map(this::toResponse);
    }

    private CarReservationRequest toCarRequest(ReservationItemRequest request) {
        return new CarReservationRequest(
                request.city(),
                request.from(),
                request.to(),
                request.category()
        );
    }

    private ReservationItemResponse toResponse(CarReservationResponse response) {
        return new ReservationItemResponse(
                response.reservationId(),
                this.getType(),
                response.category(),
                response.city(),
                response.pickup(),
                response.drop(),
                response.price()
        );
    }
}
