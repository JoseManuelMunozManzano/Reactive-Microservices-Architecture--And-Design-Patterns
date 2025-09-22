# REACTIVE MICROSERVICES ARCHITECTURE & DESIGN PATTERNS

## Project webfluxpatterns

Para crear el proyecto accederemos a `https://start.spring.io/` e indicaremos estos datos:

![alt Project Setup](./images/01-ProjectSetup.png)

## Gateway Aggregator Pattern

**¿Qué problema trata de resolver?**

Consideremos una aplicación en la que tenemos un montón de servicios como esta:

![alt Problem Statement](./images/02-GatewayAggregatorPattern01.png)

La aplicación cliente (o el navegador) tiene que obtener información de todos esos servicios backend para hacer su trabajo o construir una página.

Los problemas de este enfoque son los siguientes:

- El navegador o aplicación cliente tiene que hacer muchas llamadas por red.
- Los navegadores tienen un límite de llamadas a la vez (paralelas) a un dominio, por ejemplo, Chrome tiene un límite de 6. ¿Qué pasa si tiene que hacer 8 llamadas a servicios? Chrome hará primero 6 llamadas y una vez que cualquiera de las llamadas se complete, usará ese espacio para hacer las llamadas pendientes.
- Incremento de latencia. Imaginemos que los servidores están en USA y el cliente en Europa. Cada petición tendrá su latencia.
- Lógica de agregación compleja en el frontend. ¿Qué pasa si tenemos que añadir una característica extra introduciendo un servicio más? Tenemos que manejar esa lógica en el cliente además de la ya existente, es decir, se añade una complejidad extra.

Aquí es donde entra el patrón Agregador:

![alt Aggregator Pattern](./images/03-GatewayAggregatorPattern02.png)

El agregador es un microservicio separado cuyo trabajo es recibir las peticiones del cliente y llamar a los servicios ascendentes, recopilar la información y devolverla al cliente. Así, escode toda la complejidad del backend.

En otras palabras, actúa como un patrón Proxy o Facade.

Desde el punto de vista del cliente, solo existe una llamada, así **el cliente solo tiene que preocuparse de la lógica de presentación**.

Si tenemos que añadir una nueva característica, o tenemos que introducir uno o más microservicios, podemos asumir con seguridad que cada agregador tomará las responsabilidad de obtener la información e incluirla en la respuesta.

Consideremos esta página de detalle de producto de Amazon:

![alt Aggregator Pattern Example](./images/04-GatewayAggregatorPattern03.png)

Aquí podemos ver muchísima información sobre el producto, como el título, ratings, preguntas y respuestas, precio, ofertas promocionales, etc. Estoy seguro que no se obtiene toda esta información de un único servicio. Existirá un `productService` que contiene información del producto como el título, habrá un `pricingService`, `reviewService`, `promotionService`, etc.

Pero la página que se muestra actúa como un dashboard, combinando toda esta información proveniente de varias fuentes en una respuesta.

**Product Details Page**

Vamos a hacer un agregador parecido a la página de Amazon (parte backend).

![alt Product Details Page](./images/05-GatewayAggregatorPattern04.png)

Asumamos que somos parte de una organización en la que hay muchos equipos. Un equipo desarrolla la información de producto, otro las reviews, otro los detalles de las promociones, etc.

La parte del agregador la hace nuestro equipo, así que es lo que vamos a desarrollar en esta sección.

### External Services

Para nuestras clases del patrón Agregador, tenemos que interaccionar con estos tres servicios externos:

![alt External Services - Aggregator](./images/06-ExternalServicesAggregator.png)

- Product Service
    - Provee detalles de productos para un productId (desde id 1 hasta id 50).
- Promotion Service
    - Los productId 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 no se encuentran en este servicio.
- Review Service
    - Los productId 10, 20, 30, 40, 50 no se encuentran en este servicio. El productId 7 tiene errores extraños.

La data obtenida es aleatoria y varía de una ejecución a otra para un mismo id.

En la vida real, cada uno de estos endpoints sería un servicio jar distinto, y tendríamos que escuchar en diferentes puertos. Pero para mantenerlo simple para este curso, se han combinado todos los servicios en un único jar, aunque a nivel de estudio, los vamos a tratar como servicios distintos.

Nuestro trabajo es desarrollar el servicio `aggregator`:

![alt Job](./images/07-GatewayAggregatorPattern05.png)

El cliente nos envía una petición, por ejemplo, dame la información del producto 1. Nuestra misión es llamar a todos esos servicios externos y devolveremos la respuesta que se ve en la imagen al cliente, con ese formato. Con esto, el cliente solo se tiene que preocupar de la lógica de presentación.

### Project Setup

En `src/java/com/jmunoz/webfluxpatterns/sec01` creamos los paquetes siguientes:

- `client`
- `controller`
- `dto`
- `service`

### Creating DTO

Creamos las clases DTO que necesitamos para obtener las respuestas de los servicios externos, una clase de precios y otra con la información agrupada que devolveremos a nuestro cliente.

En `src/java/com/jmunoz/webfluxpatterns/sec01` creamos las siguientes clases:

- `dto`
  - `ProductResponse`: La respuesta que esperamos del servicio externo `Product Service`.
  - `PromotionResponse`: La respuesta que esperamos del servicio externo `Promotion Service`.
  - `Review`: La respuesta que esperamos del servicio externo `Review Service`.
  - `ProductAggregate`: Es la información agrupada que devolveremos a nuestro cliente.
  - `Price`: Información de precios con cálculos que ya tiene en cuenta promociones.
    - Evitamos que estos cálculos los haga el cliente.
    - El cliente solo se preocupa de la parte de presentación.

### Creating External Service Client

En `src/java/com/jmunoz/webfluxpatterns/sec01` creamos las siguientes clases:

- `client`
  - `ProductClient`: Llamamos a nuestro upstream service.
  - `PromotionClient`: Llamamos a nuestro upstream service.
  - `ReviewClient`: Llamamos a nuestro upstream service.

### Aggregator Service

Vamos a trabajar en la capa de servicio, para responder a lo que quiere nuestro cliente.

Nuestro servicio Aggregator sabrá a qué servicios tiene que llamar y que responden, gracias a nuestros DTOs y los service clients que ya hemos construido.

Para llamar a más de un publisher y que nos devuelvan sus resultados, usaremos el operador `zip()`.

Usamos ese operador porque las llamadas no son secuenciales. 

**NO VAMOS A HACER ESTO:** Llamamos a un servicio, por ejemplo, el de review, y esperaremos sus resultados. Luego llamamos al servicio de producto y esperamos sus resultados y por último llamamos al servicio de promociones y esperamos sus resultados. Esto afecta al rendimiento.

Con `zip()` haremos todas las llamadas en paralelo y obtendremos las respuestas de manera no bloqueante, daremos esas respuestas a nuestro aggregator service, las ensamblaremos y las devolveremos al cliente.

En `src/java/com/jmunoz/webfluxpatterns/sec01` creamos las siguientes clases:

- `service`
  - `ProductAggregatorService`

### Aggregator Controller

En `src/java/com/jmunoz/webfluxpatterns/sec01` creamos las siguientes clases:

- `controller`
  - `ProductAggregateController`

- `application.properties`: Indicamos ciertas propiedades bajo el comentario `# Aggregator Pattern (sec01)`

### Gateway Aggregator Pattern Demo

- Ejecutamos nuestro servicio externo: `java -jar external-services-v2.jar`.
- `WebfluxPatternsApplication`: Indicamos `@SpringBootApplication(scanBasePackages = "com.jmunoz.webfluxpatterns.sec01")` y ejecutamos.
- Accedemos al navegador, a la url siguiente: http://localhost:8080/sec01/product/2

El resultado esperado es este:

![alt Resultado](./images/08-GatewayAggregatorPattern06.png)

### Is our Aggregator resilient?

Vimos en la clase anterior que todo funcionó bien, pero ¿seguro que nuestro servicio funciona bien?

Si ejecutamos en el navegador esta prueba: http://localhost:8080/sec01/product/85 veremos el siguiente error:

![alt Error](./images/09-GatewayAggregatorPattern07.png)

Y en los logs vemos:

```
org.springframework.web.reactive.function.client.WebClientResponseException$NotFound: 404 Not Found from GET http://localhost:7070/sec01/product/85
	at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:324) ~[spring-webflux-6.2.10.jar:6.2.10]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ 404 NOT_FOUND from GET http://localhost:7070/sec01/product/85 [DefaultWebClient]
	*__checkpoint ⇢ Handler com.jmunoz.webfluxpatterns.sec01.controller.ProductAggregateController#getProductAggregate(Integer) [DispatcherHandler]
	*__checkpoint ⇢ HTTP GET "/sec01/product/85" [ExceptionHandlingWebHandler]
```

Nos indica un error 404 al obtener el producto 85.

Para la url http://localhost:8080/sec01/product/7 también obtenemos un error

```
org.springframework.web.reactive.function.client.WebClientResponseException$InternalServerError: 500 Internal Server Error from GET http://localhost:7070/sec01/review/7
	at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:332) ~[spring-webflux-6.2.10.jar:6.2.10]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ 500 INTERNAL_SERVER_ERROR from GET http://localhost:7070/sec01/review/7 [DefaultWebClient]
	*__checkpoint ⇢ Handler com.jmunoz.webfluxpatterns.sec01.controller.ProductAggregateController#getProductAggregate(Integer) [DispatcherHandler]
	*__checkpoint ⇢ HTTP GET "/sec01/product/7" [ExceptionHandlingWebHandler]
```

En este caso un error 500 al obtener la review 7.

Vemos que si algún servicio falla, falla todo. Vamos a corregir esto.

### Making Aggregator more resilient!

Hemos encontrado algunos problemas en nuestro servicio Aggregator. Primero tenemos que entender las razones tras estos problemas.

El operador `zip()` recolecta todos los items de los servicios publishers externos y los da como una tupla. Para que el operador `zip()` funcione, todos los publishers tienen que emitir su data.

Si un publisher falla, `zip()` emitirá la señal de error al subscriber (nuestro servicio Aggregator).

Es decir `zip()` es todo o nada.

![alt Error](./images/05-GatewayAggregatorPattern04.png)

El servicio Product es el principal. Es bueno tener las promociones y las reviews, pero si fallan con error 500 no debería dar error, y deberíamos poder construir la respuesta.

Si algo falla en el servicio de reviews o de promociones, vamos a suponer que no habían.

De esta forma, podemos hacer nuestro servicio más resiliente.

En `src/java/com/jmunoz/webfluxpatterns/sec01` corregimos las siguientes clases:

- `client`
    - `PromotionClient`: Lo hacemos más resiliente. Si ocurre algún error, suponemos que no hay promociones.
    - `ReviewClient`: Lo hacemos más resiliente. Si ocurre algún error, suponemos que no hay reviews.

Hacemos ahora las mismas pruebas:

- http://localhost:8080/sec01/product/5
  - No hay descuentos, pero funciona.
- http://localhost:8080/sec01/product/7
  - Hay un error extraño en las reviews, pero funciona porque devuelve una lista vacía de reviews.
- http://localhost:8080/sec01/product/85
  - Sigue sin funcionar porque el id no existe. Lo vemos más adelante.

Si en `application.properties` simulamos que el servicio de reviews está caído, asignando otro puerto a `sec01.review.service` y volvemos a ejecutar nuestra app, veremos que seguimos funcionando y que se devuelve una lista de reviews vacía.

Esto es resiliencia.

### Are we making parallel calls?

Vamos a hacer tests rápidos a nuestro servicio Aggregator.

¿Está haciendo realmente todas esas llamadas en paralelo? Para probar esto, al ejecutar nuestro servicio externo, vamos a incluir una propiedad para retrasar la respuesta y simular que nuestro servicio externo es lento.

- Ejecutamos nuestro servicio externo con un delay de 3sg por endpoint: `java -jar external-services-v2.jar --sec01.delay.response=3000`.
- `WebfluxPatternsApplication`: Indicamos `@SpringBootApplication(scanBasePackages = "com.jmunoz.webfluxpatterns.sec01")` y ejecutamos.
- Accedemos al navegador, a la url siguiente: http://localhost:8080/sec01/product/2

Si fuera secuencial, las tres llamadas nos tomarían 9sg, pero NO LO ES, ES PARALELO Y TARDA 3sg.

### Product Service error handling

Hemos visto que si ejecutamos esta url en el navegador: http://localhost:8080/sec01/product/85, seguimos teniendo un error 500, porque ese producto no existe.

Product Service es el servicio principal, tiene que funcionar. Si este servicio ha caído o no es accesible, no vamos a poder construir la respuesta a nuestro cliente.

Sin embargo, si ocurre un error en Product Service, tenemos que manejarlo para que no falle nuestro servicio Aggregator.

En `src/java/com/jmunoz/webfluxpatterns/sec01` corregimos las siguientes clases:

- `client`
    - `ProductClient`: Si se emite la señal de error, devolvemos empty.

**Funcionamiento**

- `ProductClient` emite la señal de error y devuelve empty.
- `ProductAggregatorService`: Como zip() recibe una señal empty devuelve la señal empty.
- `ProductAggregateController`: Como hemos recibido empty, no hay producto, así que devolvemos error 404 (notFound()).

Así se gestiona correctamente la excepción, devolviendo 404 en vez de 500.

Con esta respuesta, el cliente puede hacer una redirección a la página 404, o lo que sea.

## Scatter Gather Pattern

**¿Qué problema trata de resolver?**

![alt Problem Statement](./images/10-ScaterGatherPattern01.png)

En este patrón tendremos un servicio y recibiremos una petición del cliente. Ese servicio llamará a todos los servicios siguientes y recolectará (gather) los resultados y responderá al cliente.

A primera vista parece igual que `Gateway Aggregator Pattern`, y es parecido, pero con un caso de uso específico:

- ~aggregator
- Transmite el mensaje a todos los destinatarios (no tiene por qué ser exactamente el mismo) y luego recoge la respuesta.
- Casos de uso:
  - Buscar datos desde múltiples fuentes.
  - Dividir el trabajo y hacer procesamiento paralelo.

![alt Use Cases](./images/11-ScaterGatherPattern02.png)

Consideremos una aplicación de reserva de vuelos, algo como Google Flights, Kayak o algo así. El usuario introduce un origen y un destino y pulsa el botón para buscar vuelos.

La aplicación recibe la petición y actuará como un agente de viajes. Transmite a todos los servicios, Frontier, Delta y United, preguntando si tienen vuelos para este origen y destino, y que le envíen los detalles. Recoge todos los resultados y los devuelve a un cliente, para que seleccione el más barato, o el que sea.

El nombre Scatter indica que dispersa la petición a todos los demás servicios y recoge las respuestas.

Para entender por completo la diferencia entre `Gateway Aggregator Pattern` y `Scatter Gather Pattern` ver esta imagen:

![alt Difference](./images/12-ScaterGatherPattern03.png)

La parte izquierda corresponde al `Gateway Aggregator Pattern`. Lo que hacemos es pedir a los servicios las diferentes partes de las que consta el coche para poder ensamblarlo.

La parte derecha corresponde al `Scatter Gather Pattern`. Lo que hacemos es preguntar a los distintos servicios si tiene el coche, para que se lo envíen. Entonces elegirá el mejor, el más barato, o los recogerá todos.

### External Services

Para nuestras clases del patrón Scatter, tenemos que interaccionar con estos tres servicios externos:

![alt External Services - Scatter](./images/13-ExternalServicesScatter.png)

- Delta Service
    - No hay validaciones de from / to. Hay errores ocasionales (para realizar el manejo de excepciones)
    - El tipo de respuesta es text/event-stream
    - Probar indicando from = ATL y to = LAS
    - Probar en un navegador usando la URL siguiente: http://localhost:7070/sec02/delta/ATL/LAS para ver el resultado en formato streaming.
- Frontier Service
    - Solo acepta peticiones POST. No hay validaciones de from / to. Hay errores ocasionales (para realizar el manejo de excepciones)
    - El tipo de respuesta es text/event-stream
    - Probar indicando `{ "from": "ATL", "to": "LAS" }` 
- Jetblue Service
    - No hay validaciones de from / to. Hay errores ocasionales (para realizar el manejo de excepciones)
    - El tipo de respuesta es text/event-stream 
    - Probar indicando from = ATL y to = LAS

Tenemos tres servicios diferentes de aerolíneas. Una es Delta, la otra Frontier y la última es Jetblue. Cada uno de esos servicios devuelve distinta data.

Nuestra misión es ocultar estas complejidades usando un patrón Scatter Gather.

### Creating DTO

Creamos las clases DTO que necesitamos para obtener las respuestas de los servicios externos.

En `src/java/com/jmunoz/webfluxpatterns/sec02` creamos los paquetes/clases siguientes:

- `client`
- `controller`
- `dto`
  - `FlightResult`
- `service`

### Creating Delta Service Client

En `src/java/com/jmunoz/webfluxpatterns/sec02` creamos las clases siguientes:

- `client`
  - `DeltaClient`

### Creating JetBlue / Frontier Service Client

En `src/java/com/jmunoz/webfluxpatterns/sec02` creamos las clases siguientes:

- `client`
  - `JetBlueClient`
  - `FrontierClient`

### Creating Service

En `src/java/com/jmunoz/webfluxpatterns/sec02` creamos las clases siguientes:

- `service`
  - `FlightSearchService`

### Creating Controller

En `src/java/com/jmunoz/webfluxpatterns/sec02` creamos las clases siguientes:

- `controller`
  - `FlightsController`

No olvidar, en nuestro main, es decir, en `WebfluxPatternsApplication`, cambiar a `@SpringBootApplication(scanBasePackages = "com.jmunoz.webfluxpatterns.sec02")`.

- `application.properties`

```
sec02.delta.service=http://localhost:7070/sec02/delta/
sec02.frontier.service=http://localhost:7070/sec02/frontier/
sec02.jetblue.service=http://localhost:7070/sec02/jetblue/
```

### Scatter Gather Demo

- No olvidar ejecutar nuestro servicio externo.
- Ejecutar la app.
- Abrimos un navegador y vamos a la ruta: http://localhost:8080/sec02/flights/ATL/LAS