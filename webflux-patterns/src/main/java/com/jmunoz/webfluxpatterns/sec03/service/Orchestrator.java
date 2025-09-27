package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Orchestrator {

    // Iniciar la transacción.
    public abstract Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx);

    // Validamos si es éxito o error.
    public abstract Predicate<OrchestrationRequestContext> isSuccess();

    // Cancelamos si algo va mal.
    public abstract Consumer<OrchestrationRequestContext> cancel();
}
