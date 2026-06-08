package com.example.capstone3.Repository;

import com.example.capstone3.Enums.ContractStatus;
import com.example.capstone3.Models.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {

    Contract findContractById(Integer id);

    List<Contract> findContractsByReservation_User_Id(Integer reservationUserId);

    List<Contract> findContractsByReservation_Apartment_Owner_Id(Integer reservationApartmentOwnerId);

    List<Contract> findContractsByReservation_Apartment_IdAndContractStatus(Integer apartmentId, ContractStatus contractStatus);

    @Query("SELECT c FROM Contract c WHERE c.reservation.user.id = :userId " +
            "AND c.reservation.apartment.id = :apartmentId AND c.contractStatus = :status")
    Contract findByUserAndApartmentAndStatus(Integer userId, Integer apartmentId, ContractStatus status);

    Contract findByReservation_Id(Integer reservationId);

    boolean existsByReservation_Id(Integer reservationId);

    @Query("SELECT COUNT(c) > 0 FROM Contract c WHERE c.reservation.apartment.id = :apartmentId " +
            "AND c.contractStatus = :status")
    boolean existsByApartmentAndStatus(Integer apartmentId, ContractStatus status);

    List<Contract> findContractsByContractStatusAndEndDateBefore(ContractStatus status, LocalDate date);

    List<Contract> findContractsByContractStatusAndEndDate(ContractStatus status, LocalDate endDate);

    List<Contract> findByContractStatusAndCreatedAtBefore(ContractStatus status, LocalDateTime createdAtBefore);
}
