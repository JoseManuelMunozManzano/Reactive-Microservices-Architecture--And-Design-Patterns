package com.jmunoz.webfluxpatterns.sec05.service;

import com.jmunoz.webfluxpatterns.sec05.client.RoomClient;
import com.jmunoz.webfluxpatterns.sec05.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RoomReservationHandler extends ReservationHandler {

    private final RoomClient client;

    public RoomReservationHandler(RoomClient client) {
        this.client = client;
    }

    @Override
    protected ReservationType getType() {
        return ReservationType.ROOM;
    }

    @Override
    protected Flux<ReservationItemResponse> reserve(Flux<ReservationItemRequest> flux) {
        return flux.map(this::toRoomRequest)
                // map transforma un objeto en otro objeto.
                // transform transforma un flux en otro flux.
                .transform(this.client::reserve)
                .map(this::toResponse);
    }

    private RoomReservationRequest toRoomRequest(ReservationItemRequest request) {
        return new RoomReservationRequest(
                request.city(),
                request.from(),
                request.to(),
                request.category()
        );
    }

    private ReservationItemResponse toResponse(RoomReservationResponse response) {
        return new ReservationItemResponse(
                response.reservationId(),
                this.getType(),
                response.category(),
                response.city(),
                response.checkIn(),
                response.checkOut(),
                response.price()
        );
    }
}
