package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.client.UserClient;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class PaymentOrchestrator extends Orchestrator {

    private final UserClient client;

    public PaymentOrchestrator(UserClient client) {
        this.client = client;
    }

    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        // Iniciamos la deducción del saldo y cuando recibimos la respuesta del pago,
        // esta la pasamos a nuestro objeto de OrchestrationRequestContext y lo devolvemos.
        // Luego vamos a statusHandler (si correcto emitimos, si error lanzamos excepción).
        return this.client.deduct(ctx.getPaymentRequest())
                .doOnNext(ctx::setPaymentResponse)
                .thenReturn(ctx)
                .handle(this.statusHandler());
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        // Como hacemos llamadas secuenciales, si hubo un error en getProduct(), aquí no llegamos
        // por lo que ctx.getPaymentResponse() es null, y por tanto ctx.getPaymentResponse().status()
        // daría null pointer exception.
        //
        // return ctx -> Status.SUCCESS.equals(ctx.getPaymentResponse().status());
        //
        // Lo corregimos
        return ctx -> Objects.nonNull(ctx.getPaymentResponse()) && Status.SUCCESS.equals(ctx.getPaymentResponse().status());
    }

    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        // Si payment falla, no tengo que echar atrás payment, porque ya ha fallado,
        // es solo si otro servicio falla y el servicio de cancelación me dice que cancele.
        // Por eso el filter.
        return ctx -> Mono.just(ctx)
                .filter(isSuccess())
                .map(OrchestrationRequestContext::getPaymentRequest)
                .flatMap(this.client::refund)
                // El servicio de cancelación usará esto como un subscriber, ya que es un consumer.
                // Devolvemos Mono<Void> porque nadie lo va a consumir.
                // Nuestro orchestrator lo ejecuta de una forma completamente no bloqueante y asíncrona.
                // Como nadie se va a subscribir, esto se hace separadamente.
                .subscribe();
    }
}
