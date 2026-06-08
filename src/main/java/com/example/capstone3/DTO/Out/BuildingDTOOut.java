package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "id", "name", "ownerId", "ownerName", "city", "district", "street",
        "latitude", "longitude", "constructionYear", "totalFloors",
        "hasElevator", "hasSecurity", "hasParking", "petsAllowed"
})
@Data
public class BuildingDTOOut {

    private Integer id;
    private String name;
    private Integer ownerId;
    private String ownerName;
    private String city;
    private String district;
    private String street;
    private Double latitude;
    private Double longitude;
    private Integer constructionYear;
    private Integer totalFloors;
    private Boolean hasElevator;
    private Boolean hasSecurity;
    private Boolean hasParking;
    private Boolean petsAllowed;

}
