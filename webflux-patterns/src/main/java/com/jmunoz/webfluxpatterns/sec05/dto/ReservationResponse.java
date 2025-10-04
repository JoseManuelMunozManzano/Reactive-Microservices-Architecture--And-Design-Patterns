package com.jmunoz.webfluxpatterns.sec05.dto;

import java.util.List;
import java.util.UUID;

public record ReservationResponse(UUID reservationId,
                                  Integer price,
                                  List<ReservationItemResponse> items) {
}
