package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.client.InventoryClient;
import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec03.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        return this.client.deduct(ctx.getInventoryRequest())
                .doOnNext(ctx::setInventoryResponse)
                .thenReturn(ctx);
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        return ctx -> Status.SUCCESS.equals(ctx.getInventoryResponse().status());
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
