package com.example.capstone3.Repository;

import com.example.capstone3.Models.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceRequest, Integer> {

    MaintenanceRequest findMaintenanceRequestById(Integer id);

    List<MaintenanceRequest> findMaintenanceRequestsByUserId(Integer userId);

    List<MaintenanceRequest> findMaintenanceRequestsByApartmentId(Integer apartmentId);

    List<MaintenanceRequest> findMaintenanceRequestsByApartment_Building_Id(Integer buildingId);
}
