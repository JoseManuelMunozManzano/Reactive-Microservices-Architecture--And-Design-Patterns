package com.jmunoz.webfluxpatterns.sec01.dto;

import java.time.LocalDate;

// Product Service contiene una lista de precios, también tenemos promociones en Promotion Service.
// Para las ventas, en vez de que el cliente haga los cálculos, los haremos nosotros.
public record Price(Integer listPrice,
                    Double discount,
                    Double discountedPrice,
                    Double amountSaved,
                    LocalDate endDate) {
}
