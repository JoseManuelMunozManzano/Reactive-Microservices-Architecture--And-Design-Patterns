package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import com.jmunoz.webfluxpatterns.sec03.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderFulfillmentService {

    // En vez de indicar todos los orchestrator, usaremos una lista de orchestrators.
    // Spring los inyecta automáticamente en el constructor.
    private final List<Orchestrator> orchestrators;

    public OrderFulfillmentService(List<Orchestrator> orchestrators) {
        this.orchestrators = orchestrators;
    }

    public Mono<OrchestrationRequestContext> placeOrder(OrchestrationRequestContext ctx) {
        // Recordar que hasta que alguien no se subscriba, esto no se invoca.
        var list = orchestrators.stream()
                .map(o -> o.create(ctx))
                .toList();

        // Llamadas paralelas.
        // Usando zip, si le pasamos una lista, no devuelve las respuestas en tupla, sino en un array.
        // Como todos devuelven el mismo objeto, Mono<OrchestrationRequestContext>, nos vale con a[0].
        // Pero recordar que a[0] podría ser un entero, a[1] podría se un String, a[2] podría ser un objeto X...
        // De nuevo, como conocemos exactamente el tipo de objeto que devuelven todas las respuestas del array, usamos cast.
        //
        // En caso de múltiples threads, no tememos condiciones de carrera, ya que cada orchestrator actualiza su propio objeto
        // de OrchestrationRequestContext de forma independiente.
        // Si en OrchestrationRequestContext tuviéramos un campo private int count, cuyo valor se sumara el algunos
        // orchestrators y se restara en otros, entonces si habría problemas de condiciones de carrera, pero, de nuevo,
        // teniendo cada orchestrator su propio campo en OrchestrationRequestContext, no hay problema.
        return Mono.zip(list, a -> a[0])
                .cast(OrchestrationRequestContext.class)
                .doOnNext(this::updateStatus);
    }

    private void updateStatus(OrchestrationRequestContext ctx) {
        var allSuccess = this.orchestrators.stream().allMatch(o -> o.isSuccess().test(ctx));
        var status = allSuccess ? Status.SUCCESS : Status.FAILED;
        ctx.setStatus(status);
    }
}
