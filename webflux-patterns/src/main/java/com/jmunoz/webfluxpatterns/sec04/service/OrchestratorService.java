package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.dto.*;
import com.jmunoz.webfluxpatterns.sec04.util.DebugUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrchestratorService {

    private final OrderFulfillmentService fulfillmentService;
    private final OrderCancellationService cancellationService;

    public OrchestratorService(OrderFulfillmentService fulfillmentService, OrderCancellationService cancellationService) {
        this.fulfillmentService = fulfillmentService;
        this.cancellationService = cancellationService;
    }

    // Creamos el objeto de tipo OrchestrationRequestContext.
    // Luego creamos los objetos request de OrchestrationRequestContext (buildRequestContext).
    // Luego hacemos las tres llamadas secuenciales (placeOrder).
    // Luego, una vez obtenemos el resultado, hacemos el postproceso para ver si tenemos que cancelar (esto es asíncrono)
    // Luego, escribimos los logs.
    // Luego respondemos al cliente.
    //
    // Si el producto no está disponible, se emitirá la señal empty y to-do será empty y fulfillmentService::placeOrder
    // no llegará a invocarse.
    // Se devuelve 404.
    public Mono<OrderResponse> placeOrder(Mono<OrderRequest> mono) {
        return mono
                .map(OrchestrationRequestContext::new)
                // Si hay un error, ya viene manejado en OrderFulfillmentService.
                // Es decir, no recibimos señal de error, sino el context object indicando Status.FAILED
                .flatMap(fulfillmentService::placeOrder)
                .doOnNext(this::doOrderPostProcessing)
                .doOnNext(DebugUtil::print) // solo para debug.
                .map(this::toOrderResponse);
    }

    // Esta es la lógica que hacemos si to-do va bien o algo va mal.
    // Si es una lógica muy compleja, esto podría ser una interface separada.
    private void doOrderPostProcessing(OrchestrationRequestContext ctx) {
        if (Status.FAILED.equals(ctx.getStatus())) {
            this.cancellationService.cancelOrder(ctx);
        }
    }

    private OrderResponse toOrderResponse(OrchestrationRequestContext ctx) {
        var isSuccess = Status.SUCCESS.equals(ctx.getStatus());
        var address = isSuccess ? ctx.getShippingResponse().address() : null;
        var deliveryDate = isSuccess ? ctx.getShippingResponse().expectedDelivery() : null;

        return new OrderResponse(
                ctx.getOrderRequest().userId(),
                ctx.getOrderRequest().productId(),
                ctx.getOrderId(),
                ctx.getStatus(),
                address,
                deliveryDate
        );
    }
}
