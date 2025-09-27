package com.jmunoz.webfluxpatterns.sec03.service;

import com.jmunoz.webfluxpatterns.sec03.dto.OrchestrationRequestContext;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class OrderCancellationService {

    private Sinks.Many<OrchestrationRequestContext> sink;
    private Flux<OrchestrationRequestContext> flux;

    // En vez de indicar todos los orchestrator, usaremos una lista de orchestrators.
    // Spring los inyecta automáticamente en el constructor.
    private final List<Orchestrator> orchestrators;

    public OrderCancellationService(List<Orchestrator> orchestrators) {
        this.orchestrators = orchestrators;
    }

    // En vez de @PostConstruct, también puedo crear una clase de configuración anotada con @Configuration
    // donde expondríamos como beans:
    //    private Sinks.Many<OrchestrationRequestContext> sink;
    //    private Flux<OrchestrationRequestContext> flux;
    //  Y en esta clase les haríamos autowired por el constructor, por ejemplo.
    //
    // multicast porque cada orchestrator tiene que subscribirse.
    // En vez de Schedulers.boundedElastic() podríamos haber dedicado un thread pool también.
    // Se subscriben los orchestrators, y recordar que o.cancel() es un consumer de OrchestrationRequestContext.
    @PostConstruct
    public void init() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.flux = this.sink.asFlux().publishOn(Schedulers.boundedElastic());
        orchestrators.forEach(o -> this.flux.subscribe(o.cancel()));
    }


    // Este proceso puede llevar tiempo (no lo sabemos) pero no bloquearemos al cliente.
    // Por eso será completamente asíncrono y usamos un sink, que se encargará del
    // proceso de cancelación por nosotros.
    public void cancelOrder(OrchestrationRequestContext ctx) {
        this.sink.tryEmitNext(ctx);
    }
}
