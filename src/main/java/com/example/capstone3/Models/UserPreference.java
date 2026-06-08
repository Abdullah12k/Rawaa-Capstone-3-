package com.example.capstone3.Models;

import com.example.capstone3.Enums.PreferenceLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lazy because full user details are not needed when loading preferences.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "work_latitude")
    private Double workLatitude;

    @Column(name = "work_longitude")
    private Double workLongitude;

    @Column(name = "workplace_name", length = 200)
    private String workplaceName;

    @Column(name = "max_budget", nullable = false)
    private Double maxBudget;

    @Column(name = "requires_parking")
    private Boolean requiresParking;

    @Column(name = "requires_elevator")
    private Boolean requiresElevator;

    @Column(name = "requires_furnished")
    private Boolean requiresFurnished;

    @Column(name = "lookingForRoommate")
    private Boolean lookingForRoommate;

    @Column(name = "roommateBudget")
    private Double roommateBudget;

    // instead of boolean and to be very accurate
    // enumtype.string so its entered as string
    @Enumerated(EnumType.STRING)
    @Column(name = "gym_preference", nullable = false, length = 20)
    private PreferenceLevel gymPreference = PreferenceLevel.NOT_IMPORTANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "cafes_preference", nullable = false, length = 20)
    private PreferenceLevel cafesPreference = PreferenceLevel.NOT_IMPORTANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "hospital_preference", nullable = false, length = 20)
    private PreferenceLevel hospitalPreference = PreferenceLevel.NOT_IMPORTANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_preference", nullable = false, length = 20)
    private PreferenceLevel schoolPreference = PreferenceLevel.NOT_IMPORTANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "public_transport_preference", nullable = false, length = 20)
    private PreferenceLevel publicTransportPreference = PreferenceLevel.NOT_IMPORTANT;

    @Column(name = "preferred_bedrooms")
    private Integer preferredBedrooms;

    @Column(name = "preferred_bathrooms")
    private Integer preferredBathrooms;

    @Column(name = "preferred_district", length = 100)
    private String preferredDistrict;
}
