package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "rank", "apartmentId", "apartmentNumber", "district", "monthlyRent",
        "bedrooms", "bathrooms", "area", "furnished",
        "commuteDistanceKm", "commuteMinutes", "totalScore"
})
@Data
public class RankedApartmentDTOOut {

    private Integer rank;
    private Integer apartmentId;
    private String apartmentNumber;
    private String district;
    private Double monthlyRent;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private Boolean furnished;
    private Double commuteDistanceKm;
    private Integer commuteMinutes;
    private Double totalScore;
    @JsonIgnore
    private Boolean waterIncluded;
    @JsonIgnore
    private Boolean electricityIncluded;
    @JsonIgnore
    private Boolean internetIncluded;
    @JsonIgnore
    private Boolean buildingHasParking;
    @JsonIgnore
    private Boolean buildingHasElevator;
    @JsonIgnore
    private Boolean buildingHasSecurity;
    @JsonIgnore
    private Double budgetScore;
    @JsonIgnore
    private Double amenityScore;
    @JsonIgnore
    private Double commuteScore;
    @JsonIgnore
    private Double familyScore;
    @JsonIgnore
    private Double apartmentScore;
    @JsonIgnore
    private Double averageRating;
    @JsonIgnore
    private ApartmentServicesDTOOut nearbyServices;
}
