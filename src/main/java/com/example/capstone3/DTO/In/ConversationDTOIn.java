package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationDTOIn {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Owner ID is required")
    private Integer ownerId;

    @NotNull(message = "Apartment ID is required")
    private Integer apartmentId;
}
