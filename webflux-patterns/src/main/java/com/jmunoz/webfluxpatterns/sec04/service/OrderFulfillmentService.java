package com.jmunoz.webfluxpatterns.sec04.service;

import com.jmunoz.webfluxpatterns.sec04.client.ProductClient;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.dto.Product;
import com.jmunoz.webfluxpatterns.sec04.dto.Status;
import com.jmunoz.webfluxpatterns.sec04.util.OrchestrationUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderFulfillmentService {

    // Inyectamos cada orchestrator sobre los que haremos las llamadas secuenciales.
    private final ProductClient productClient;
    private final PaymentOrchestrator paymentOrchestrator;
    private final InventoryOrchestrator inventoryOrchestrator;
    private final ShippingOrchestrator shippingOrchestrator;

    public OrderFulfillmentService(ProductClient productClient, PaymentOrchestrator paymentOrchestrator, InventoryOrchestrator inventoryOrchestrator, ShippingOrchestrator shippingOrchestrator) {
        this.productClient = productClient;
        this.paymentOrchestrator = paymentOrchestrator;
        this.inventoryOrchestrator = inventoryOrchestrator;
        this.shippingOrchestrator = shippingOrchestrator;
    }

    public Mono<OrchestrationRequestContext> placeOrder(OrchestrationRequestContext ctx) {
        // Llamadas secuenciales.
        return this.getProduct(ctx)
                .doOnNext(OrchestrationUtil::buildPaymentRequest)
                .flatMap(this.paymentOrchestrator::create)
                .doOnNext(OrchestrationUtil::buildInventoryRequest)
                .flatMap(this.inventoryOrchestrator::create)
                .doOnNext(OrchestrationUtil::buildShippingRequest)
                .flatMap(this.shippingOrchestrator::create)
                // Si va bien.
                .doOnNext(c -> c.setStatus(Status.SUCCESS))
                // Manejamos el error (si alguno de los doOnNext de arriba da error, no continua los siguientes, va aquí)
                // No continuamos emitiendo el error hacia el llamador OrchestratorService y devolvemos ctx.
                .doOnError(ex -> ctx.setStatus(Status.FAILED))
                .onErrorReturn(ctx);
    }

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
}
