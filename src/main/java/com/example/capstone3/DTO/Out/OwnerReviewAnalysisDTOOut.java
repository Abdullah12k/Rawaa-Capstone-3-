package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"analysis"})
@Data
public class OwnerReviewAnalysisDTOOut {

    private String analysis;
}
