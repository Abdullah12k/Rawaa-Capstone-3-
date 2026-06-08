package com.example.capstone3.DTO.Out;

import com.example.capstone3.Enums.PreferenceLevel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "id", "userId", "workLatitude", "workLongitude", "workplaceName",
        "maxBudget", "requiresParking", "requiresElevator", "requiresFurnished",
        "gymPreference", "cafesPreference", "hospitalPreference", "schoolPreference",
        "publicTransportPreference", "preferredBedrooms", "preferredBathrooms",
        "preferredDistrict", "lookingForRoommate", "roommateBudget"
})
@Data
public class UserPreferenceDTOOut {

    private Integer id;
    private Integer userId;
    private Double workLatitude;
    private Double workLongitude;
    private String workplaceName;
    private Double maxBudget;
    private Boolean requiresParking;
    private Boolean requiresElevator;
    private Boolean requiresFurnished;
    private PreferenceLevel gymPreference;
    private PreferenceLevel cafesPreference;
    private PreferenceLevel hospitalPreference;
    private PreferenceLevel schoolPreference;
    private PreferenceLevel publicTransportPreference;
    private Integer preferredBedrooms;
    private Integer preferredBathrooms;
    private String preferredDistrict;
    private Boolean lookingForRoommate;
    private Double roommateBudget;
}
