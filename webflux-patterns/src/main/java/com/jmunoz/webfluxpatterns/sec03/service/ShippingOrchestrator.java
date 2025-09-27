package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.client.ShippingClient;
import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec03.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
                .thenReturn(ctx);
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        return ctx -> Status.SUCCESS.equals(ctx.getShippingResponse().status());
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
