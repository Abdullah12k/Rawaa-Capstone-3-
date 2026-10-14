package com.example.capstone3.Repository;

import com.example.capstone3.Models.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
    boolean existsByIdAndOwner_Id(Integer id, Integer ownerId);

    Building findBuildingById(Integer id);
}
