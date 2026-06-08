package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"id", "apartmentNumber", "district", "monthlyRent",
        "totalReservations", "totalCancellations", "cancellationRate", "averageRate"
})
@Data
public class FlaggedApartmentDTOOut {

    private Integer id;
    private String apartmentNumber;
    private String district;
    private Double monthlyRent;
    private Integer totalReservations;
    private Integer totalCancellations;
    private Double cancellationRate;
    private Double averageRate;
}
