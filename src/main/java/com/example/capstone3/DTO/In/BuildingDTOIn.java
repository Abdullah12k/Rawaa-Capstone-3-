package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BuildingDTOIn {


    @NotEmpty(message = "Building name is required")
    @Size(max = 150, message = "Building name must not exceed 150 characters")
    private String name;

    @NotEmpty(message = "District is required")
    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @NotEmpty(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotEmpty(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Min(value = 1800, message = "Construction year is invalid")
    @Max(value = 2100, message = "Construction year is invalid")
    private Integer constructionYear;
    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;
    private Boolean hasElevator;
    private Boolean hasSecurity;
    private Boolean hasParking;
    private Boolean petsAllowed;
}
