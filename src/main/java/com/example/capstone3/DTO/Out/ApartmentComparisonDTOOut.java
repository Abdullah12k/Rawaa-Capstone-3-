package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"comparison"})
@Data
public class ApartmentComparisonDTOOut {

    private String comparison;
}
