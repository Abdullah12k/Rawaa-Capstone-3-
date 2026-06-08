package com.example.capstone3.DTO.Out;

import com.example.capstone3.Enums.ApartmentStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;

@JsonPropertyOrder({
        "id", "apartmentNumber", "buildingId", "ownerId", "district",
        "monthlyRent", "negotiable", "bedrooms", "bathrooms", "area",
        "floorNumber", "furnished", "availableFrom", "allowedTenantType",
        "waterIncluded", "internetIncluded", "electricityIncluded", "status"
})
@Data
public class ApartmentDTOOut {

    private Integer id;
    private String apartmentNumber;
    private Integer buildingId;
    private Integer ownerId;
    private String district;
    private Double monthlyRent;
    private Boolean negotiable;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private Integer floorNumber;
    private Boolean furnished;
    private LocalDate availableFrom;
    private String allowedTenantType;
    private Boolean waterIncluded;
    private Boolean internetIncluded;
    private Boolean electricityIncluded;
    private ApartmentStatus status;
}
