package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"district", "nearbyCounts", "summary"})
@Data
public class NeighborhoodSummaryDTOOut {

    private String district;
    private NearbyCounts nearbyCounts;
    private String summary;

    @JsonPropertyOrder({"schools", "supermarkets", "restaurants", "hospitals"})
    @Data
    public static class NearbyCounts {
        private int schools;
        private int supermarkets;
        private int restaurants;
        private int hospitals;
    }
}
