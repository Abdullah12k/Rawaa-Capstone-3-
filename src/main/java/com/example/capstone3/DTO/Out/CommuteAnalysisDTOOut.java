package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"distanceKm", "durationMinutes"})
@Data
public class CommuteAnalysisDTOOut {

    private double distanceKm;
    private int durationMinutes;
}
