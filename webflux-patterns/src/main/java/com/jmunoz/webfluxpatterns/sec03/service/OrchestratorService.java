package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.client.ProductClient;
import com.jmunoz.webfluxpatterns.sec03.dto.*;
import com.jmunoz.webfluxpatterns.sec03.util.DebugUtil;
import com.jmunoz.webfluxpatterns.sec03.util.OrchestrationUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrchestratorService {

    private final ProductClient productClient;
    private final OrderFulfillmentService fulfillmentService;
    private final OrderCancellationService cancellationService;

    public OrchestratorService(ProductClient productClient, OrderFulfillmentService fulfillmentService, OrderCancellationService cancellationService) {
        this.productClient = productClient;
        this.fulfillmentService = fulfillmentService;
        this.cancellationService = cancellationService;
    }

    // Creamos el objeto de tipo OrchestrationRequestContext.
    // Lo primero es hacer la petición al servicio product para obtener la información del producto.
    // Luego creamos los objetos request de OrchestrationRequestContext (buildRequestContext).
    // Luego hacemos las tres llamadas en paralelo (placeOrder).
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
                .flatMap(this::getProduct)
                .doOnNext(OrchestrationUtil::buildRequestContext)
                .flatMap(fulfillmentService::placeOrder)
                .doOnNext(this::doOrderPostProcessing)
                .doOnNext(DebugUtil::print) // solo para debug.
                .map(this::toOrderResponse);
    }

    // De producto solo necesitamos el precio.
    private Mono<OrchestrationRequestContext> getProduct(OrchestrationRequestContext ctx) {
        return this.productClient.getProduct(ctx.getOrderRequest().productId())
                // Si se devuelve la señal empty, no se hace el map ni el doOnNext, por lo que price es null.
                .map(Product::price)
                .doOnNext(ctx::setProductPrice)
                // Este thenReturn se hace siempre, devolviendo ctx.
                // Pero debería devolver la señal onComplete si tenemos empty.
                // .thenReturn(ctx);
                // Cambiamos el thenReturn(ctx) por este map.
                // Si el price está, seguimos con ctx por el pipeline.
                // Si el price no está (empty signal) entonces este map no se hace y, cuando vuelve al pipeline
                // seguimos con la señal empty en el méto-do placeOrder.
                .map(i -> ctx);
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
