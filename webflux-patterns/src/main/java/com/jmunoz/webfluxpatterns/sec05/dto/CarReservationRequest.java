package com.jmunoz.webfluxpatterns.sec05.dto;

import java.time.LocalDate;

public record CarReservationRequest(String city,
                                     LocalDate pickup,
                                     LocalDate drop,
                                     String category) {
}
