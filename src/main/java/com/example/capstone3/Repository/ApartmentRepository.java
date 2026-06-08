package com.example.capstone3.Repository;

import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Enums.ApartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Integer> {

    Apartment findApartmentById(Integer id);

    List<Apartment> findApartmentsByOwnerId(Integer ownerId);

    List<Apartment> findByStatus(ApartmentStatus status);

    boolean existsByBuilding_IdAndApartmentNumberIgnoreCase(
            Integer buildingId, String apartmentNumber);

    boolean existsByBuilding_IdAndApartmentNumberIgnoreCaseAndIdNot(
            Integer buildingId, String apartmentNumber, Integer apartmentId);

}
