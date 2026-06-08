package com.example.capstone3.DTO.In;

import com.example.capstone3.Enums.PreferenceLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.*;
import com.example.capstone3.Enums.PreferenceLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPreferenceDTOIn {

    @DecimalMin(value = "-90.0", message = "Work latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Work latitude must be <= 90")
    private Double workLatitude;

    @DecimalMin(value = "-180.0", message = "Work longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Work longitude must be <= 180")
    private Double workLongitude;

    @NotNull(message = "Max budget is required")
    @Positive(message = "Max budget must be a positive number")
    private Double maxBudget;

    private Boolean requiresParking;
    private Boolean requiresElevator;
    private Boolean requiresFurnished;

    private PreferenceLevel gymPreference;
    private PreferenceLevel cafesPreference;
    private PreferenceLevel hospitalPreference;
    private PreferenceLevel schoolPreference;
    private PreferenceLevel publicTransportPreference;

    @Min(value = 1, message = "Preferred bedrooms must be at least 1")
    private Integer preferredBedrooms;
    @Min(value = 1, message = "Preferred bathrooms must be at least 1")
    private Integer preferredBathrooms;

    @Size(max = 100, message = "Preferred district must not exceed 100 characters")
    private String preferredDistrict;

    @NotNull(message = "lookingForRoommate status is required")
    private Boolean lookingForRoommate;

    @Positive(message = "Roommate budget must be a positive number")
    private Double roommateBudget;

}
