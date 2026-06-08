package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"summary"})
@Data
public class BuildingMaintenanceSummaryDTOOut {

    private String summary;
}
