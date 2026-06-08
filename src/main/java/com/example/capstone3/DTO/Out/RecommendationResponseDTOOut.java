package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@JsonPropertyOrder({"rankedApartments", "recommendation"})
@Data
public class RecommendationResponseDTOOut {

    private List<RankedApartmentDTOOut> rankedApartments;
    private String recommendation;
}
