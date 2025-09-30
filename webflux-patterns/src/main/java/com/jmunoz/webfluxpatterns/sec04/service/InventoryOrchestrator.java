package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.client.InventoryClient;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class InventoryOrchestrator extends Orchestrator {

    private final InventoryClient client;

    public InventoryOrchestrator(InventoryClient client) {
        this.client = client;
    }

    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        // Iniciamos la deducción del inventario y cuando recibimos la respuesta del mismo,
        // este lo pasamos a nuestro objeto de OrchestrationRequestContext y lo devolvemos.
        // Luego vamos a statusHandler (si correcto emitimos, si error lanzamos excepción).
        return this.client.deduct(ctx.getInventoryRequest())
                .doOnNext(ctx::setInventoryResponse)
                .thenReturn(ctx)
                .handle(this.statusHandler());
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        // Como hacemos llamadas secuenciales, si hubo un error en PaymentOrchestrator, aquí no llegamos
        // por lo que ctx.getInventoryResponse() es null, y por tanto ctx.getInventoryResponse().status()
        // daría null pointer exception.
        //
        // return ctx -> Status.SUCCESS.equals(ctx.getInventoryResponse().status());
        //
        // Lo corregimos
        return ctx -> Objects.nonNull(ctx.getInventoryResponse()) && Status.SUCCESS.equals(ctx.getInventoryResponse().status());
    }

    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        return ctx -> Mono.just(ctx)
                .filter(isSuccess())
                .map(OrchestrationRequestContext::getInventoryRequest)
                .flatMap(this.client::restore)
                .subscribe();
    }
}
