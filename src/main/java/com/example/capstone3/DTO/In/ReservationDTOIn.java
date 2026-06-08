package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationDTOIn {

    @NotNull(message = "Requested start date is required")
    @FutureOrPresent(message = "Requested start date must be today or in the future")
    private LocalDate requestedStartDate;

    @NotNull(message = "Rental months is required")
    @Positive(message = "Rental months must be greater than zero")
    private Integer rentalMonths;
}
