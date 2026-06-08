package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkplaceDTOIn {

    @NotEmpty(message = "Workplace name is required")
    @Size(max = 200, message = "Workplace name must not exceed 200 characters")
    private String workplaceName;
}
