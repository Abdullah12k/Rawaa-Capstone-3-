package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"candidateId", "candidateName", "matchPercentage", "reason"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoommateMatchDTOOut {
    private Integer candidateId;
    private String candidateName; // We will map this manually for safety
    private Integer matchPercentage;
    private String reason;
}
