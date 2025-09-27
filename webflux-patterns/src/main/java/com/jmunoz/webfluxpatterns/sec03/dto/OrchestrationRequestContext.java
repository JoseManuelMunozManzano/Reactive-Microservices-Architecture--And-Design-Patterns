package com.jmunoz.webfluxpatterns.sec03.dto;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

// Una sola petición tiene que tratar con muchas otras peticiones y respuestas.
// Esta clase wrapper contiene las referencias a dichas peticiones y respuestas.
// Esta clase también nos facilita la vida en caso de debug.
@Data
@ToString
public class OrchestrationRequestContext {

    // En la vida real este orderId sería creado por un order service que nos devolvería este UUID.
    // En este ejemplo lo creamos con valor aleatorio.
    private final UUID orderId = UUID.randomUUID();
    private OrderRequest orderRequest;
    // No necesitamos Product completo, solo el precio.
    private Integer productPrice;
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;
    private InventoryRequest inventoryRequest;
    private InventoryResponse inventoryResponse;
    private ShippingRequest shippingRequest;
    private ShippingResponse shippingResponse;
    // Para saber si ha sido exitoso o no.
    private Status status;

    // La administración de este request context se creará basada en OrderRequest.
    public OrchestrationRequestContext(OrderRequest orderRequest) {
        this.orderRequest = orderRequest;
    }
}
