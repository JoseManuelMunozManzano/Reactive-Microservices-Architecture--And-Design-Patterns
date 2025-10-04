package com.jmunoz.webfluxpatterns.sec05.service;

import com.jmunoz.webfluxpatterns.sec05.dto.ReservationItemRequest;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationItemResponse;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationResponse;
import com.jmunoz.webfluxpatterns.sec05.dto.ReservationType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

// Dependiendo del tipo que viene en la petición, se llama al ReservationHandler correspondiente.
@Service
public class ReservationService {

    private final Map<ReservationType, ReservationHandler> map;

    // Spring puede cargar automáticamente la lista de ReservationHandler.
    // Pasamos de una lista a una estructura Map.
    // Con esto puedo obtener, basado en el tipo, el ReservationHandler concreto.
    public ReservationService(List<ReservationHandler> list) {
        this.map = list.stream().collect(Collectors.toMap(
                ReservationHandler::getType,
                Function.identity()  // Este es el objeto ReservationHandler en sí.
        ));
    }

    // Lo que acabamos devolviendo al cliente es el wrapper ReservationResponse.
    public Mono<ReservationResponse> reserve(Flux<ReservationItemRequest> flux) {
        // Ver explicación de groupBy en el README, bajo el subtítulo ### Reservation Service.
        return flux.groupBy(ReservationItemRequest::type) // Por lo que queremos dividir en varios Flux.
                .flatMap(this::aggregator)
                .collectList()  // Aquí pasamos de Flux a un Mono que contiene una lista.
                .map(this::toResponse);
    }

    // GroupedFlux extiende de Flux (es un Flux) y tiene un méto-do extra key() que indica que tipo de Flux es,
    // por el que se agrupan sus elementos.
    // En nuestro caso, key() tiene el ReservationType.
    private Flux<ReservationItemResponse> aggregator(GroupedFlux<ReservationType, ReservationItemRequest> groupedFlux) {
        var key = groupedFlux.key();
        var handler = map.get(key);
        return handler.reserve(groupedFlux);    // Reservamos!!
    }

    private ReservationResponse toResponse(List<ReservationItemResponse> list) {
        return new ReservationResponse(
                UUID.randomUUID(),
                list.stream().mapToInt(ReservationItemResponse::price).sum(),
                list
        );
    }
}
