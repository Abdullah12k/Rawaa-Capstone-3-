package com.example.capstone3.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lazy because owner details are not always needed when loading a building.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "city", nullable = false, length = 100)
    private String city;


    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // Lazy because apartment collections are not needed for basic building responses.
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Apartment> apartments = new ArrayList<>();

    @Column(name = "construction_year")
    private Integer constructionYear;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "has_elevator")
    private Boolean hasElevator;

    @Column(name = "has_security")
    private Boolean hasSecurity;

    @Column(name = "has_parking")
    private Boolean hasParking;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed;



}
