package com.jmunoz.webfluxpatterns.sec03.util;

import com.jmunoz.webfluxpatterns.sec03.dto.InventoryRequest;
import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec03.dto.PaymentRequest;
import com.jmunoz.webfluxpatterns.sec03.dto.ShippingRequest;

// Crea los objetos request de OrchestrationRequestContext.
// Se puede crear como un componente de Spring o directamente exponer los métodos.
// Hacemos esto último.
public class OrchestrationUtil {

    public static void buildRequestContext(OrchestrationRequestContext ctx) {
        buildPaymentRequest(ctx);
        buildInventoryRequest(ctx);
        buildShippingRequest(ctx);
    }

    private static void buildPaymentRequest(OrchestrationRequestContext ctx) {
        var paymentRequest = new PaymentRequest(
                ctx.getOrderRequest().userId(),
                ctx.getProductPrice() * ctx.getOrderRequest().quantity(),
                ctx.getOrderId()
        );
        ctx.setPaymentRequest(paymentRequest);
    }

    private static void buildInventoryRequest(OrchestrationRequestContext ctx) {
        var inventoryRequest = new InventoryRequest(
                ctx.getOrderId(),
                ctx.getOrderRequest().productId(),
                ctx.getOrderRequest().quantity()
        );
        ctx.setInventoryRequest(inventoryRequest);
    }

    private static void buildShippingRequest(OrchestrationRequestContext ctx) {
        var shippingRequest = new ShippingRequest(
                ctx.getOrderRequest().quantity(),
                ctx.getOrderRequest().userId(),
                ctx.getOrderId()
        );
        ctx.setShippingRequest(shippingRequest);
    }
}
