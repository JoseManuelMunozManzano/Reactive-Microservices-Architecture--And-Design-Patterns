package com.jmunoz.webfluxpatterns.sec05.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationItemResponse(UUID itemId,
                                      ReservationType type,
                                      String category,
                                      String city,
                                      LocalDate from,
                                      LocalDate to,
                                      Integer price) {
}
