package com.jmunoz.webfluxpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Indicamos scanBasePackages para que solo cargue los paquetes bajo la sección indicada.
// Sustituir sec01 por sec02, sec03... en función del patrón que deseemos ejecutar/probar.
@SpringBootApplication(scanBasePackages = "com.jmunoz.webfluxpatterns.sec05")
public class WebfluxPatternsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxPatternsApplication.class, args);
	}

}
