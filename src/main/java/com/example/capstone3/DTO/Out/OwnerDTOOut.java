package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({"id", "fullName", "email", "phoneNumber", "commercialRegistrationNumber"})
@Data
public class OwnerDTOOut {

    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String commercialRegistrationNumber;
}
