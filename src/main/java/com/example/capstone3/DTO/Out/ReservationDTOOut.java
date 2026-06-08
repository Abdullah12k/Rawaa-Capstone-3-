package com.example.capstone3.DTO.Out;

import com.example.capstone3.Enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;

@JsonPropertyOrder({
        "id", "apartmentId", "userId", "status", "requestedStartDate", "rentalMonths"
})
@Data
public class ReservationDTOOut {

    private Integer id;
    private Integer apartmentId;
    private Integer userId;
    private ReservationStatus status;
    private LocalDate requestedStartDate;
    private Integer rentalMonths;
}
