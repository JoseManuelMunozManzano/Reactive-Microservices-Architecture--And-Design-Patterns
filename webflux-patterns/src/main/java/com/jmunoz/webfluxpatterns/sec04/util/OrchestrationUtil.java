package com.jmunoz.webfluxpatterns.sec04.util;

import com.jmunoz.webfluxpatterns.sec04.dto.InventoryRequest;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec04.dto.PaymentRequest;
import com.jmunoz.webfluxpatterns.sec04.dto.ShippingRequest;

public class OrchestrationUtil {

    public static void buildPaymentRequest(OrchestrationRequestContext ctx) {
        var paymentRequest = new PaymentRequest(
                ctx.getOrderRequest().userId(),
                ctx.getProductPrice() * ctx.getOrderRequest().quantity(),
                ctx.getOrderId()
        );
        ctx.setPaymentRequest(paymentRequest);
    }

    public static void buildInventoryRequest(OrchestrationRequestContext ctx) {
        var inventoryRequest = new InventoryRequest(
                // Necesitamos el id generado en el servicio payment.
                ctx.getPaymentResponse().paymentId(),
                ctx.getOrderRequest().productId(),
                ctx.getOrderRequest().quantity()
        );
        ctx.setInventoryRequest(inventoryRequest);
    }

    public static void buildShippingRequest(OrchestrationRequestContext ctx) {
        var shippingRequest = new ShippingRequest(
                ctx.getOrderRequest().quantity(),
                ctx.getOrderRequest().userId(),
                // Necesitamos el id generado en el servicio inventory.
                ctx.getInventoryResponse().inventoryId()
        );
        ctx.setShippingRequest(shippingRequest);
    }
}
