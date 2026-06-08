package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.ReservationDTOIn;
import com.example.capstone3.DTO.Out.ReservationDTOOut;
import com.example.capstone3.Enums.ApartmentStatus;
import com.example.capstone3.Enums.ContractStatus;
import com.example.capstone3.Enums.ReservationStatus;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Contract;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.Reservation;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.ContractRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.ReservationRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    // Pending reservations expire after two days without an owner decision.
    private static final long PENDING_RESERVATION_EXPIRY_DAYS = 2;

    // Approved reservations expire after three days if no contract is generated.
    private static final long APPROVED_WITHOUT_CONTRACT_EXPIRY_DAYS = 3;

    // Only reservations that can still lead to a contract block a duplicate request.
    private static final List<ReservationStatus> OPEN_RESERVATION_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);

    private final ReservationRepository reservationRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ContractRepository contractRepository;
    private final WhatsAppService whatsAppService;

    public List<ReservationDTOOut> getAll() {
        List<ReservationDTOOut> reservationDTOOuts = new ArrayList<>();
        for (Reservation reservation : reservationRepository.findAll()) {
            reservationDTOOuts.add(convertToDTO(reservation));
        }
        return reservationDTOOuts;
    }

    public ReservationDTOOut getReservation(Integer id) { // add user id validation
        Reservation reservation = reservationRepository.findReservationById(id);
        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }
        return convertToDTO(reservation);
    }

    @Transactional
    public ReservationDTOOut addReservation(Integer userId, Integer apartmentId, Double desiredMonthlyRent, ReservationDTOIn reservationDTOIn ) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (apartment.getStatus() != ApartmentStatus.AVAILABLE) {
            throw new ApiException("Apartment is not available for reservation");
        }
        if (apartment.getAvailableFrom() != null
                && reservationDTOIn.getRequestedStartDate().isBefore(apartment.getAvailableFrom())) {
            throw new ApiException("Requested start date must be on or after the apartment available date");
        }
        if (reservationRepository.existsByApartment_IdAndStatus(apartment.getId(), ReservationStatus.APPROVED)) {
            throw new ApiException("Apartment already has an approved reservation");
        }
        if (contractRepository.existsByApartmentAndStatus(
                apartment.getId(), ContractStatus.ACTIVE)) {
            throw new ApiException("Apartment already has an active contract");
        }
        if (apartment.getAllowedTenantType() != null
                && !apartment.getAllowedTenantType().isBlank()) {
            String allowedTenantType =
                    apartment.getAllowedTenantType().trim().toLowerCase();
            boolean familyTenant =
                    (user.getChildrenCount() != null && user.getChildrenCount() > 0)
                            || (user.getFamilyCount() != null
                            && user.getFamilyCount() > 1);

            if (allowedTenantType.contains("family") && !familyTenant) {
                throw new ApiException("This apartment is restricted to family tenants");
            }
            if ((allowedTenantType.contains("single")) && familyTenant) {
                throw new ApiException("This apartment is restricted to single tenants");
            }
        }
        if (reservationRepository.existsByUser_IdAndApartment_IdAndStatusIn(userId, apartmentId, OPEN_RESERVATION_STATUSES)) {
            throw new ApiException("You already have an open reservation for this apartment");
        }
        if (Boolean.TRUE.equals(apartment.getNegotiable())
                && desiredMonthlyRent != null) {
            apartment.setDesiredMonthlyRent(desiredMonthlyRent);
            apartmentRepository.save(apartment);
        }
        Reservation reservation = new Reservation();
        reservation.setApartment(apartment);
        reservation.setUser(user);
        reservation.setRequestedStartDate(reservationDTOIn.getRequestedStartDate());
        reservation.setRentalMonths(reservationDTOIn.getRentalMonths());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        whatsAppService.notifyOwnerNewReservation(apartment, reservation);
        return convertToDTO(reservation);
    }

    public void updateReservation(Integer id, ReservationDTOIn reservationDTOIn) {
        Reservation reservation = reservationRepository.findReservationById(id);
        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ApiException("Only pending reservations can be updated");
        }
        Apartment apartment = reservation.getApartment();
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        User user = reservation.getUser();
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (apartment.getAvailableFrom() != null
                && reservationDTOIn.getRequestedStartDate().isBefore(apartment.getAvailableFrom())) {
            throw new ApiException("Requested start date must be on or after the apartment available date");
        }
        reservation.setApartment(apartment);
        reservation.setUser(user);
        reservation.setRequestedStartDate(reservationDTOIn.getRequestedStartDate());
        reservation.setRentalMonths(reservationDTOIn.getRentalMonths());
        reservationRepository.save(reservation);
    }

    public void deleteReservation(Integer id) {
        Reservation reservation = reservationRepository.findReservationById(id);
        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ApiException("Only pending reservations can be deleted");
        }
        reservationRepository.deleteById(id);
    }

    public ReservationDTOOut convertToDTO(Reservation reservation) {
        ReservationDTOOut reservationDTOOut = new ReservationDTOOut();
        reservationDTOOut.setId(reservation.getId());
        reservationDTOOut.setApartmentId(reservation.getApartment().getId());
        reservationDTOOut.setUserId(reservation.getUser().getId());
        reservationDTOOut.setRequestedStartDate(reservation.getRequestedStartDate());
        reservationDTOOut.setRentalMonths(reservation.getRentalMonths());
        reservationDTOOut.setStatus(reservation.getStatus());
        return reservationDTOOut;
    }

    public List<ReservationDTOOut> getPendingReservations(){
        List<Reservation> reservations = reservationRepository.findReservationsByStatus(ReservationStatus.PENDING);

        if(reservations.isEmpty()){
            throw new ApiException("No pending reservations were found!");
        }

        List<ReservationDTOOut> reservationDTOOuts = new ArrayList<>();

        for (Reservation reservation : reservations) {
            reservationDTOOuts.add(convertToDTO(reservation));
        }

        return reservationDTOOuts;
    }


    public List<ReservationDTOOut> getOwnerPendingReservations(Integer ownerId){
        Owner owner = ownerRepository.findOwnerById(ownerId);

        if (owner == null) {
            throw new ApiException("Owner not found");
        }

        List<Reservation> reservations = reservationRepository.findReservationsByStatusAndApartment_OwnerId(ReservationStatus.PENDING, ownerId);

        if(reservations.isEmpty()){
            throw new ApiException("No pending reservations for this owner were found!");
        }

        List<ReservationDTOOut> reservationDTOOuts = new ArrayList<>();

        for (Reservation reservation : reservations) {
            reservationDTOOuts.add(convertToDTO(reservation));
        }

        return reservationDTOOuts;
    }


    public List<ReservationDTOOut> getReservationsByUserId(Integer userId){
        User user = userRepository.findUserById(userId);

        if(user == null){
            throw new ApiException("User not found!");
        }

        List<Reservation> reservations = reservationRepository.findReservationsByUser_Id(userId);

        if(reservations.isEmpty()){
            throw new ApiException("No reservations for this user were found!");
        }

        List<ReservationDTOOut> reservationDTOOuts = new ArrayList<>();

        for (Reservation reservation : reservations) {
            reservationDTOOuts.add(convertToDTO(reservation));
        }

        return reservationDTOOuts;
    }


    @Transactional
    public void acceptReservation(Integer ownerId, Integer reservationId) {
        Reservation initialReservation = reservationRepository.findReservationById(reservationId);
        if (initialReservation == null) {
            throw new ApiException("Reservation not found");
        }
        Apartment apartment = apartmentRepository.findApartmentById(
                initialReservation.getApartment().getId());
        Reservation reservation = reservationRepository.findReservationById(reservationId);

        if (!apartment.getOwner().getId().equals(ownerId)) {
            throw new ApiException("You do not have permission to accept this reservation");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ApiException("Only pending reservations can be accepted");
        }

        if (apartment.getStatus() != ApartmentStatus.AVAILABLE) {
            throw new ApiException("Apartment is no longer available");
        }
        if (reservationRepository.existsByApartment_IdAndStatusAndIdNot(apartment.getId(), ReservationStatus.APPROVED, reservationId)) {
            throw new ApiException("Apartment already has an approved reservation");
        }
        if (contractRepository.existsByApartmentAndStatus(
                apartment.getId(), ContractStatus.ACTIVE)) {
            throw new ApiException("Apartment already has an active contract");
        }
        reservation.setStatus(ReservationStatus.APPROVED);
        reservation.setApprovedAt(LocalDateTime.now());
        apartment.setStatus(ApartmentStatus.RESERVED);

        if (Boolean.TRUE.equals(apartment.getNegotiable()) && apartment.getDesiredMonthlyRent() != null) {
            apartment.setMonthlyRent(apartment.getDesiredMonthlyRent());
        }

        for (Reservation competingReservation :
                reservationRepository.findReservationsByApartment_IdAndStatus(
                        apartment.getId(), ReservationStatus.PENDING)) {
            if (!competingReservation.getId().equals(reservationId)) {
                competingReservation.setStatus(ReservationStatus.REJECTED);
            }
        }

        reservationRepository.save(reservation);
        apartmentRepository.save(apartment);

        whatsAppService.notifyTenantReservationAccepted(reservation.getUser(), apartment);
    }


    @Transactional
    public void rejectReservation(Integer ownerId, Integer reservationId) {
        Reservation reservation = reservationRepository.findReservationById(reservationId);

        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }

        // Security Check
        if (!reservation.getApartment().getBuilding().getOwner().getId().equals(ownerId)) {
            throw new ApiException("You do not have permission to reject this reservation");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ApiException("Only pending reservations can be rejected");
        }

        Contract contract = contractRepository.findByReservation_Id(reservation.getId());
        if (contract != null && contract.getContractStatus() == ContractStatus.ACTIVE) {
            throw new ApiException("An active rental must be ended through the contract");
        }
        if (contract != null && contract.getContractStatus() == ContractStatus.PENDING) {
            contract.setSigned(false);
            contract.setContractStatus(ContractStatus.CANCELLED);
            contractRepository.save(contract);
        }
        reservation.setStatus(ReservationStatus.REJECTED);
        reservationRepository.save(reservation);

        Apartment apartment = reservation.getApartment();
        whatsAppService.notifyTenantReservationRejected(reservation.getUser(), apartment);

    }

    // check
    @Transactional
    public void endReservation(Integer userId, Integer reservationId) {
        Reservation reservation = reservationRepository.findReservationById(reservationId);

        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }

        // Security Check: Is this the user who made the reservation?
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ApiException("You can only cancel your own reservations");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new ApiException("Only pending or approved reservations can be cancelled");
        }

        Contract contract = contractRepository.findByReservation_Id(reservationId);
        if (contract != null && contract.getContractStatus() == ContractStatus.ACTIVE) {
            throw new ApiException("An active rental must be ended through the contract");
        }

        boolean wasApproved = reservation.getStatus() == ReservationStatus.APPROVED;
        if (contract != null && contract.getContractStatus() == ContractStatus.PENDING) {
            contract.setSigned(false);
            contract.setContractStatus(ContractStatus.CANCELLED);
            contractRepository.save(contract);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        if (wasApproved) {
            Apartment apartment = reservation.getApartment();
            boolean canReleaseApartment = apartment.getStatus() == ApartmentStatus.RESERVED
                            && !contractRepository.existsByApartmentAndStatus(
                            apartment.getId(), ContractStatus.ACTIVE)
                            && !reservationRepository
                            .existsByApartment_IdAndStatusAndIdNot(
                                    apartment.getId(),
                                    ReservationStatus.APPROVED,
                                    reservation.getId());
            if (canReleaseApartment) {
                apartment.setStatus(ApartmentStatus.AVAILABLE);
                apartmentRepository.save(apartment);
            }
        }

        whatsAppService.notifyOwnerReservationCancelled(
                reservation.getApartment().getOwner(), reservation);
    }

//    @Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    @Transactional
    public void checkReservationExpiration() {

        LocalDateTime pendingExpirationTime =
                LocalDateTime.now().minusDays(PENDING_RESERVATION_EXPIRY_DAYS);

        List<Reservation> expiredReservations =
                reservationRepository.findByStatusAndCreatedAtBefore(
                        ReservationStatus.PENDING, pendingExpirationTime);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            whatsAppService.notifyTenantReservationExpired(
                    reservation.getUser(), reservation.getApartment());
        }

        LocalDateTime approvedExpirationTime = LocalDateTime.now()
                .minusDays(APPROVED_WITHOUT_CONTRACT_EXPIRY_DAYS);
        List<Reservation> approvedWithoutContract =
                reservationRepository.findByStatusAndApprovedAtBefore(
                        ReservationStatus.APPROVED, approvedExpirationTime);

        for (Reservation candidate : approvedWithoutContract) {
            Apartment apartment = apartmentRepository.findApartmentById(
                    candidate.getApartment().getId());
            Reservation reservation =
                    reservationRepository.findReservationById(candidate.getId());

            if (reservation.getStatus() != ReservationStatus.APPROVED
                    || contractRepository.findByReservation_Id(reservation.getId()) != null) {
                continue;
            }

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            boolean canReleaseApartment = apartment.getStatus() == ApartmentStatus.RESERVED
                    && !contractRepository.existsByApartmentAndStatus(
                            apartment.getId(), ContractStatus.ACTIVE)
                    && !reservationRepository.existsByApartment_IdAndStatusAndIdNot(
                            apartment.getId(),
                            ReservationStatus.APPROVED,
                            reservation.getId());

            if (canReleaseApartment) {
                apartment.setStatus(ApartmentStatus.AVAILABLE);
                apartmentRepository.save(apartment);
            }

            whatsAppService.notifyTenantReservationExpired(
                    reservation.getUser(), apartment);
        }

    }

}
