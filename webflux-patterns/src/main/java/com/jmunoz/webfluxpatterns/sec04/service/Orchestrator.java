package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.exception.OrderFulfillmentFailure;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Orchestrator {

    // Iniciar la transacción.
    public abstract Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx);

    // Validamos si es éxito o error.
    public abstract Predicate<OrchestrationRequestContext> isSuccess();

    // Cancelamos si algo va mal.
    public abstract Consumer<OrchestrationRequestContext> cancel();

    // Si va bien, emitimos el objeto.
    // Si va mal se emite la excepción y paramos el pipeline.
    protected BiConsumer<OrchestrationRequestContext, SynchronousSink<OrchestrationRequestContext>> statusHandler() {
        return (ctx, sink) -> {
            if (isSuccess().test(ctx)) {
                sink.next(ctx);
            } else {
                sink.error(new OrderFulfillmentFailure());
            }
        };
    }
}
