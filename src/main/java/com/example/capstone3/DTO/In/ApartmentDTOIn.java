package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApartmentDTOIn {

    @NotEmpty(message = "Apartment number is required")
    @Size(max = 50, message = "Apartment number must not exceed 50 characters")
    private String apartmentNumber;

    @NotNull(message = "Monthly rent is required")
    @Positive(message = "Monthly rent must be positive")
    private Double monthlyRent;

    @NotNull(message = "negotiable is required")
    private Boolean negotiable;

    @NotNull(message = "Number of bedrooms is required")
    @Positive(message = "Bedrooms must be a positive number")
    private Integer bedrooms;

    @NotNull(message = "Number of bathrooms is required")
    @Positive(message = "Bathrooms must be a positive number")
    private Integer bathrooms;

    @NotNull(message = "Area is required")
    @Positive(message = "Area must be positive")
    private Double area;

    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floorNumber;
    private Boolean furnished;
    private java.time.LocalDate availableFrom;
    @Size(max = 50, message = "Allowed tenant type must not exceed 50 characters")
    private String allowedTenantType;
    private Boolean waterIncluded;
    private Boolean internetIncluded;
    private Boolean electricityIncluded;

    // Status defaults to AVAILABLE on creation not accepted from client
}
