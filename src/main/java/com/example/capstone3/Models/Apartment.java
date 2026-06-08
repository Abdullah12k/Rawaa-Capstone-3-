package com.example.capstone3.Models;

import com.example.capstone3.Enums.ApartmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(
        name = "apartments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_apartment_building_number",
                columnNames = {"building_id", "apartment_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lazy because building details are not always needed when loading an apartment.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    // Lazy because owner details are not always needed when loading an apartment.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "apartment_number", nullable = false, length = 50)
    private String apartmentNumber;

    @Column(name = "monthly_rent", nullable = false)
    private Double monthlyRent;

    @Column(name = "desired_monthly_rent")
    private Double desiredMonthlyRent;

    @Column(name = "negotiable")
    private Boolean negotiable;

    @Column(name = "bedrooms", nullable = false)
    private Integer bedrooms;

    @Column(name = "bathrooms", nullable = false)
    private Integer bathrooms;

    @Column(name = "area", nullable = false)
    private Double area;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApartmentStatus status;

    @Column(name = "furnished")
    private Boolean furnished;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "allowed_tenant_type", length = 50)
    private String allowedTenantType;

    @Column(name = "water_included")
    private Boolean waterIncluded;

    @Column(name = "internet_included")
    private Boolean internetIncluded;

    @Column(name = "electricity_included")
    private Boolean electricityIncluded;

    // Lazy because reservation history is not needed for most apartment operations.
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    // Lazy because reviews are loaded only for review and analysis operations.
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    // Lazy because maintenance requests are loaded only for maintenance workflows.
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    // Lazy because conversations are not needed when loading apartment details.
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conversation> conversations = new ArrayList<>();
}
