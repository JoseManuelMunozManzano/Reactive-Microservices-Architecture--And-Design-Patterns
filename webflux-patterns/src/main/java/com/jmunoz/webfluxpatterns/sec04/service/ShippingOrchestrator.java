package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.client.ShippingClient;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class ShippingOrchestrator extends Orchestrator {

    private final ShippingClient client;

    public ShippingOrchestrator(ShippingClient client) {
        this.client = client;
    }

    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        return this.client.schedule(ctx.getShippingRequest())
                .doOnNext(ctx::setShippingResponse)
                .thenReturn(ctx)
                .handle(this.statusHandler());
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        // Como hacemos llamadas secuenciales, si hubo un error en PaymentOrchestrator, aquí no llegamos
        // por lo que ctx.getShippingResponse() es null, y por tanto ctx.getShippingResponse().status()
        // daría null pointer exception.
        //
        // return ctx -> Status.SUCCESS.equals(ctx.getShippingResponse().status());
        //
        // Lo corregimos.
        return ctx -> Objects.nonNull(ctx.getShippingResponse()) && Status.SUCCESS.equals(ctx.getShippingResponse().status());
    }

    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        return ctx -> Mono.just(ctx)
                .filter(isSuccess())
                .map(OrchestrationRequestContext::getShippingRequest)
                .flatMap(this.client::cancel)
                .subscribe();
    }
}
