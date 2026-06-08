package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "id", "apartmentNumber", "district", "monthlyRent", "bedrooms",
        "bathrooms", "area", "totalReservations", "avgDaysToReserve",
        "averageRating", "score"
})
@Data
public class UnderpricedApartmentDTOOut {

    private Integer id;
    private String apartmentNumber;
    private String district;
    private Double monthlyRent;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;

    private Integer totalReservations;       // High demand
    private Double avgDaysToReserve;         // Fast reservations
    private Double averageRating;            // Good reviews

    private Double score;                    // Final weighted score
}
