package com.example.capstone3.Repository;

import com.example.capstone3.Models.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {

    Building findBuildingById(Integer id);
}
