package com.jmunoz.webfluxpatterns.sec05.dto;

import java.time.LocalDate;

public record ReservationItemRequest(ReservationType type,
                                     String category,
                                     String city,
                                     LocalDate from,
                                     LocalDate to) {
}
