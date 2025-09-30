package com.jmunoz.webfluxpatterns.sec04.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmunoz.webfluxpatterns.sec04.dto.OrchestrationRequestContext;

// No es que los logs se tengan que implementar siempre así en producción.
// Esto es por motivos de aprendizaje.
// Es útil cuando tratamos con muchos objetos y servicios.
public class DebugUtil {

    public static void print(OrchestrationRequestContext ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ctx));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
