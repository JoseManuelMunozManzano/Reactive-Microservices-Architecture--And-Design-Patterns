package com.jmunoz.webfluxpatterns.sec08.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Sobreescribir la configuración de CircuitBreaker dada en application.yaml.
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreakerConfigCustomizer reviewService() {
        // Indicamos el nombre de la instancia, la misma que tenemos en application.yaml para sobreescribirla.
        // También hay que indicar un consumer de builder y es ahí donde sobreescribimos las propiedades que queramos.
        return CircuitBreakerConfigCustomizer.of("review-service", builder ->
                builder.minimumNumberOfCalls(4)
        );
    }
}
