package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ContractDTOIn {

    @Positive(message = "Security deposit must be positive")
    private Double securityDeposit;

    // Status defaults to ACTIVE on creation not accepted from client
}
