package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "hospitalCount", "schoolCount", "supermarketCount",
        "pharmacyCount", "gymCount", "restaurantCount"
})
@Data
public class ApartmentServicesDTOOut {

    private int hospitalCount;
    private int schoolCount;
    private int supermarketCount;
    private int pharmacyCount;
    private int gymCount;
    private int restaurantCount;
}
