package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.ApartmentDTOIn;
import com.example.capstone3.DTO.Out.*;
import com.example.capstone3.Enums.ApartmentStatus;
import com.example.capstone3.Enums.ContractStatus;
import com.example.capstone3.Enums.ReservationStatus;
import com.example.capstone3.Models.*;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.BuildingRepository;
import com.example.capstone3.Repository.ContractRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final OwnerRepository ownerRepository;
    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;
    private final AiService aiService;

    public List<ApartmentDTOOut> getAll() {
        List<ApartmentDTOOut> apartmentDTOOuts = new ArrayList<>();
        for (Apartment apartment : apartmentRepository.findAll()) {
            apartmentDTOOuts.add(convertToDTO(apartment));
        }
        return apartmentDTOOuts;
    }

    public ApartmentDTOOut getApartment(Integer id) {
        Apartment apartment = apartmentRepository.findApartmentById(id);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        return convertToDTO(apartment);
    }

    public void addApartment(Integer ownerId, Integer buildingId, ApartmentDTOIn apartmentDTOIn) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        Building building = buildingRepository.findBuildingById(buildingId);
        if (building == null) {
            throw new ApiException("Building not found");
        }
        if (!building.getOwner().getId().equals(owner.getId())) {
            throw new ApiException("Owner does not own this building");
        }
        String apartmentNumber = apartmentDTOIn.getApartmentNumber().trim();
        if (apartmentRepository.existsByBuilding_IdAndApartmentNumberIgnoreCase(
                buildingId, apartmentNumber)) {
            throw new ApiException("Apartment number already exists in this building");
        }
        Apartment apartment = new Apartment();
        apartment.setBuilding(building);
        apartment.setOwner(owner);
        apartment.setApartmentNumber(apartmentNumber);
        apartment.setMonthlyRent(apartmentDTOIn.getMonthlyRent());
        apartment.setNegotiable(apartmentDTOIn.getNegotiable());
        apartment.setBedrooms(apartmentDTOIn.getBedrooms());
        apartment.setBathrooms(apartmentDTOIn.getBathrooms());
        apartment.setArea(apartmentDTOIn.getArea());
        apartment.setFloorNumber(apartmentDTOIn.getFloorNumber());
        apartment.setFurnished(apartmentDTOIn.getFurnished());
        apartment.setAvailableFrom(apartmentDTOIn.getAvailableFrom());
        apartment.setAllowedTenantType(apartmentDTOIn.getAllowedTenantType());
        apartment.setWaterIncluded(apartmentDTOIn.getWaterIncluded());
        apartment.setInternetIncluded(apartmentDTOIn.getInternetIncluded());
        apartment.setElectricityIncluded(apartmentDTOIn.getElectricityIncluded());
        apartment.setStatus(ApartmentStatus.AVAILABLE);
        apartmentRepository.save(apartment);
    }

    public void updateApartment(Integer id, ApartmentDTOIn apartmentDTOIn) {
        Apartment apartment = apartmentRepository.findApartmentById(id);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        if (apartment.getStatus() == ApartmentStatus.RESERVED
                || apartment.getStatus() == ApartmentStatus.RENTED) {
            throw new ApiException("Reserved or rented apartments cannot be updated");
        }
        String apartmentNumber = apartmentDTOIn.getApartmentNumber().trim();
        if (apartmentRepository.existsByBuilding_IdAndApartmentNumberIgnoreCaseAndIdNot(
                apartment.getBuilding().getId(), apartmentNumber, apartment.getId())) {
            throw new ApiException("Apartment number already exists in this building");
        }
        apartment.setApartmentNumber(apartmentNumber);
        apartment.setMonthlyRent(apartmentDTOIn.getMonthlyRent());
        apartment.setBedrooms(apartmentDTOIn.getBedrooms());
        apartment.setBathrooms(apartmentDTOIn.getBathrooms());
        apartment.setArea(apartmentDTOIn.getArea());
        apartment.setFloorNumber(apartmentDTOIn.getFloorNumber());
        apartment.setFurnished(apartmentDTOIn.getFurnished());
        apartment.setAvailableFrom(apartmentDTOIn.getAvailableFrom());
        apartment.setAllowedTenantType(apartmentDTOIn.getAllowedTenantType());
        apartment.setWaterIncluded(apartmentDTOIn.getWaterIncluded());
        apartment.setInternetIncluded(apartmentDTOIn.getInternetIncluded());
        apartment.setElectricityIncluded(apartmentDTOIn.getElectricityIncluded());
        apartmentRepository.save(apartment);
    }

    public void deleteApartment(Integer id) {
        Apartment apartment = apartmentRepository.findApartmentById(id);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        if (!apartment.getReservations().isEmpty()
                || !apartment.getReviews().isEmpty()
                || !apartment.getMaintenanceRequests().isEmpty()
                || apartment.getReservations().stream()
                .anyMatch(reservation -> reservation.getContract() != null)) {
            throw new ApiException(
                    "Apartment has rental history and cannot be deleted. Mark it INACTIVE instead");
        }
        apartmentRepository.deleteById(id);
    }

    public ApartmentDTOOut convertToDTO(Apartment apartment) {
        ApartmentDTOOut apartmentDTOOut = new ApartmentDTOOut();
        apartmentDTOOut.setId(apartment.getId());
        apartmentDTOOut.setBuildingId(apartment.getBuilding().getId());
        apartmentDTOOut.setOwnerId(apartment.getOwner().getId());
        apartmentDTOOut.setDistrict(apartment.getBuilding().getDistrict());
        apartmentDTOOut.setApartmentNumber(apartment.getApartmentNumber());
        apartmentDTOOut.setMonthlyRent(apartment.getMonthlyRent());
        apartmentDTOOut.setNegotiable(apartment.getNegotiable());
        apartmentDTOOut.setBedrooms(apartment.getBedrooms());
        apartmentDTOOut.setBathrooms(apartment.getBathrooms());
        apartmentDTOOut.setArea(apartment.getArea());
        apartmentDTOOut.setFloorNumber(apartment.getFloorNumber());
        apartmentDTOOut.setStatus(apartment.getStatus());
        apartmentDTOOut.setFurnished(apartment.getFurnished());
        apartmentDTOOut.setAvailableFrom(apartment.getAvailableFrom());
        apartmentDTOOut.setAllowedTenantType(apartment.getAllowedTenantType());
        apartmentDTOOut.setWaterIncluded(apartment.getWaterIncluded());
        apartmentDTOOut.setInternetIncluded(apartment.getInternetIncluded());
        apartmentDTOOut.setElectricityIncluded(apartment.getElectricityIncluded());

        return apartmentDTOOut;
    }


    //^^^^^^^CRUD^^^^^^^^


    public List<UnderpricedApartmentDTOOut> getUnderpricedApartments() {
        List<Apartment> apartments = apartmentRepository.findAll();
        List<UnderpricedApartmentDTOOut> result = new ArrayList<>();

        for (Apartment apartment : apartments) {

            // --- Average days to get reserved (Speed) ---
            List<Reservation> reservations = apartment.getReservations();
            double avgDaysToReserve = 0;
            if (!reservations.isEmpty()) {
                long totalDays = 0;
                for (Reservation reservation : reservations) {
                    totalDays += ChronoUnit.DAYS.between(
                            reservation.getCreatedAt().toLocalDate(),
                            reservation.getRequestedStartDate());
                }
                avgDaysToReserve = (double) totalDays / reservations.size();
            }

            // --- Average rating (Reviews) ---
            List<Review> reviews = apartment.getReviews();
            double avgRating = 0;
            if (!reviews.isEmpty()) {
                double totalRating = 0;
                for (Review review : reviews) {
                    totalRating += review.getRating();
                }
                avgRating = totalRating / reviews.size();
            }

            // --- Filter: must have reservations and good reviews ---
            if (reservations.isEmpty() || avgRating < 4.0) continue;

            // --- Weighted score: Speed(20%) + Rating(50%) + Demand(30%) ---
            double speedScore   = 1.0 / (1.0 + avgDaysToReserve);   // fewer days = higher score
            double ratingScore  = avgRating / 5.0;                   // normalize to 0-1
            double demandScore  = Math.min(reservations.size() / 10.0, 1.0); // cap at 10 reservations

            double finalScore = (speedScore * 0.20) + (ratingScore * 0.50) + (demandScore * 0.30);

            UnderpricedApartmentDTOOut dto = new UnderpricedApartmentDTOOut();
            dto.setId(apartment.getId());
            dto.setApartmentNumber(apartment.getApartmentNumber());
            dto.setDistrict(apartment.getBuilding().getDistrict());
            dto.setMonthlyRent(apartment.getMonthlyRent());
            dto.setBedrooms(apartment.getBedrooms());
            dto.setBathrooms(apartment.getBathrooms());
            dto.setArea(apartment.getArea());
            dto.setTotalReservations(reservations.size());
            dto.setAvgDaysToReserve(Math.round(avgDaysToReserve * 100.0) / 100.0);
            dto.setAverageRating(Math.round(avgRating * 100.0) / 100.0);
            dto.setScore(Math.round(finalScore * 100.0) / 100.0);
            result.add(dto);
        }

        if (result.isEmpty()) {
            throw new ApiException("No underpriced apartments found");
        }

        result.sort(Comparator.comparingDouble(UnderpricedApartmentDTOOut::getScore).reversed());
        return result;
    }


    public List<LowRatedApartmentDTOOut> getLowRatedApartmentsByBuilding(Integer ownerId, Integer buildingId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }

        Building building = buildingRepository.findBuildingById(buildingId);
        if (building == null) {
            throw new ApiException("Building not found");
        }

        // Security check: owner must own this building
        if (!building.getOwner().getId().equals(ownerId)) {
            throw new ApiException("You do not have permission to view this building");
        }

        List<LowRatedApartmentDTOOut> result = new ArrayList<>();

        for (Apartment apartment : building.getApartments()) {

            List<Review> reviews = apartment.getReviews();
            if (reviews.isEmpty()) continue;

            // Calculate average rating
            double totalRating = 0;
            for (Review review : reviews) {
                totalRating += review.getRating();
            }
            double avgRating = totalRating / reviews.size();

            // Only include apartments with average rating below 2.0
            if (avgRating >= 2.0) continue;

            // Build comments string to send to AI
            StringBuilder comments = new StringBuilder();
            for (Review review : reviews) {
                comments.append("- ").append(review.getComment()).append("\n");
            }

            // Call AI to summarize the issues
            String aiSummary = getAiSummary(apartment.getApartmentNumber(), comments.toString());

            LowRatedApartmentDTOOut dto = new LowRatedApartmentDTOOut();
            dto.setId(apartment.getId());
            dto.setApartmentNumber(apartment.getApartmentNumber());
            dto.setDistrict(building.getDistrict());
            dto.setMonthlyRent(apartment.getMonthlyRent());
            dto.setBedrooms(apartment.getBedrooms());
            dto.setBathrooms(apartment.getBathrooms());
            dto.setAverageRating(Math.round(avgRating * 100.0) / 100.0);
            dto.setAiSummary(aiSummary);
            result.add(dto);
        }

        if (result.isEmpty()) {
            throw new ApiException("No low rated apartments found in this building");
        }

        result.sort(Comparator.comparingDouble(LowRatedApartmentDTOOut::getAverageRating));
        return result;
    }

    public List<FlaggedApartmentDTOOut> getFlaggedApartmentsByCancellationRate(Integer ownerId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }

        List<Apartment> apartments = apartmentRepository.findApartmentsByOwnerId(ownerId);
        if (apartments.isEmpty()) {
            throw new ApiException("No apartments found for this owner");
        }

        // Step 1: Calculate cancellation rate for each apartment
        // Rate = total cancellations / total reservations
        List<FlaggedApartmentDTOOut> allApartmentStats = new ArrayList<>();
        for (Apartment apartment : apartments) {
            int total = apartment.getReservations().size();
            if (total == 0) continue; // skip apartments with no reservations

            int cancelled = 0;
            for (Reservation r : apartment.getReservations()) {
                if (r.getStatus() == ReservationStatus.CANCELLED) cancelled++;
            }

            double rate = (double) cancelled / total;

            FlaggedApartmentDTOOut dto = new FlaggedApartmentDTOOut();
            dto.setId(apartment.getId());
            dto.setApartmentNumber(apartment.getApartmentNumber());
            dto.setDistrict(apartment.getBuilding().getDistrict());
            dto.setMonthlyRent(apartment.getMonthlyRent());
            dto.setTotalReservations(total);
            dto.setTotalCancellations(cancelled);
            dto.setCancellationRate(Math.round(rate * 100.0) / 100.0);
            allApartmentStats.add(dto);
        }

        // Step 2: Calculate the average cancellation rate across all apartments
        // This is used as a baseline to detect outliers
        double averageRate = allApartmentStats.stream()
                .mapToDouble(FlaggedApartmentDTOOut::getCancellationRate)
                .average().orElse(0);

        // Step 3: Flag apartments whose cancellation rate is more than 2x the average
        // 2x is used to identify significant outliers, not just slightly above average
        List<FlaggedApartmentDTOOut> flagged = new ArrayList<>();
        for (FlaggedApartmentDTOOut dto : allApartmentStats) {
            if (dto.getCancellationRate() > averageRate * 2) {
                dto.setAverageRate(Math.round(averageRate * 100.0) / 100.0);
                flagged.add(dto);
            }
        }

        if (flagged.isEmpty()) {
            throw new ApiException("No flagged apartments found");
        }

        // Step 4: Sort by cancellation rate descending so worst apartments appear first
        flagged.sort(Comparator.comparingDouble(FlaggedApartmentDTOOut::getCancellationRate).reversed());
        return flagged;
    }

    private String getAiSummary(String apartmentNumber, String comments) {
        String prompt = "You are analyzing tenant reviews for apartment number \"" + apartmentNumber + "\".\n" +
                "Here are the review comments:\n" + comments + "\n" +
                "Summarize the main issues tenants are complaining about in one sentence starting with \"Main issues: \"";
        return aiService.generateText(prompt, "EN");
    }


    public void toggleMaintenanceMode(Integer ownerId, Integer apartmentId){
        Owner owner = ownerRepository.findOwnerById(ownerId);
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);

        if(owner == null){
            throw new ApiException("Owner not found");
        }

        if (apartment == null) {
            throw new ApiException("Apartment not found!");
        }

        if(!apartment.getOwner().getId().equals(ownerId)){
            throw new ApiException("You are not authorized to this action!");
        }
        if (apartment.getStatus() != ApartmentStatus.AVAILABLE) {
            throw new ApiException("Only available apartments can be placed under maintenance");
        }

        apartment.setStatus(ApartmentStatus.UNDER_MAINTENANCE);
        apartmentRepository.save(apartment);
    }


    public void toggleAvailableMode(Integer ownerId, Integer apartmentId){
        Owner owner = ownerRepository.findOwnerById(ownerId);
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);

        if(owner == null){
            throw new ApiException("Owner not found");
        }

        if (apartment == null) {
            throw new ApiException("Apartment not found!");
        }

        if(!apartment.getOwner().getId().equals(ownerId)){
            throw new ApiException("You are not authorized to this action!");
        }
        if (apartment.getStatus() != ApartmentStatus.UNDER_MAINTENANCE) {
            throw new ApiException("Only apartments under maintenance can be made available");
        }
        if (contractRepository.existsByApartmentAndStatus(
                apartmentId, ContractStatus.ACTIVE)
                || reservationRepository.existsByApartment_IdAndStatus(
                apartmentId, ReservationStatus.APPROVED)) {
            throw new ApiException("Apartment still has an active rental lock");
        }

        apartment.setStatus(ApartmentStatus.AVAILABLE);
        apartmentRepository.save(apartment);
    }

    public Map<String, Object> getNextAvailability(Integer apartmentId) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apartmentId", apartment.getId());
        response.put("status", apartment.getStatus());

        LocalDate today = LocalDate.now();
        if (apartment.getStatus() == ApartmentStatus.AVAILABLE) {
            boolean availableNow = apartment.getAvailableFrom() == null
                    || !apartment.getAvailableFrom().isAfter(today);
            response.put("expectedAvailableDate", availableNow ? null : apartment.getAvailableFrom());
            return response;
        }

        List<Contract> activeContracts =
                contractRepository.findContractsByReservation_Apartment_IdAndContractStatus(
                        apartmentId, ContractStatus.ACTIVE);
        if (!activeContracts.isEmpty()) {
            LocalDate expectedDate = activeContracts.stream()
                    .map(Contract::getEndDate)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            response.put("expectedAvailableDate", expectedDate);
            return response;
        }

        response.put("expectedAvailableDate", apartment.getAvailableFrom());
        return response;
    }

    public Map<String, Object> checkAvailabilityOnDate(Integer apartmentId, LocalDate date) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ApiException("Availability date cannot be in the past");
        }

        boolean activeContractBlocksDate = contractRepository.findContractsByReservation_Apartment_IdAndContractStatus(
                                apartmentId, ContractStatus.ACTIVE)
                        .stream()
                        .anyMatch(contract -> !date.isBefore(contract.getStartDate())
                                && !date.isAfter(contract.getEndDate()));

        boolean pendingContractBlocksDate = contractRepository.findContractsByReservation_Apartment_IdAndContractStatus(
                                apartmentId, ContractStatus.PENDING)
                        .stream()
                        .anyMatch(contract -> !date.isBefore(contract.getStartDate())
                                && !date.isAfter(contract.getEndDate()));

        boolean reservationBlocksDate =
                reservationRepository.findReservationsByApartment_IdAndStatus(
                                apartmentId, ReservationStatus.PENDING)
                        .stream()
                        .anyMatch(reservation -> date.equals(reservation.getRequestedStartDate()))
                        || reservationRepository.findReservationsByApartment_IdAndStatus(
                                apartmentId, ReservationStatus.APPROVED)
                        .stream()
                        .anyMatch(reservation -> date.equals(reservation.getRequestedStartDate()));

        boolean available = apartment.getStatus() == ApartmentStatus.AVAILABLE
                && (apartment.getAvailableFrom() == null
                || !apartment.getAvailableFrom().isAfter(date))
                && !activeContractBlocksDate
                && !pendingContractBlocksDate
                && !reservationBlocksDate;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apartmentId", apartment.getId());
        response.put("date", date);
        response.put("status", apartment.getStatus());

        return response;
    }

    // this method show the owner apartments and their status. The apartments will be grouped by status.
    public Map<ApartmentStatus, List<ApartmentDTOOut>> getOwnerDashboard(Integer ownerId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);

        if (owner == null) {
            throw new ApiException("Owner not found!");
        }

        List<Apartment> apartments = apartmentRepository.findApartmentsByOwnerId(ownerId);

        Map<ApartmentStatus, List<ApartmentDTOOut>> groupedApartments = new HashMap<>();

        for (ApartmentStatus status : ApartmentStatus.values()) {
            groupedApartments.put(status, new ArrayList<>());
        }

        for (Apartment apartment : apartments) {
            ApartmentDTOOut dto = convertToDTO(apartment);
            groupedApartments.get(apartment.getStatus()).add(dto);
        }

        return groupedApartments;
    }

    public List<ApartmentDTOOut> searchApartments(Double minRent, Double maxRent, Integer bedrooms, String district, Boolean isFurnished) {
        if ((minRent != null && minRent < 0) || (maxRent != null && maxRent < 0)) {
            throw new ApiException("Rent filters cannot be negative");
        }
        if (minRent != null && maxRent != null && minRent > maxRent) {
            throw new ApiException("Minimum rent cannot be greater than maximum rent");
        }

        List<Apartment> availableApartments = apartmentRepository.findByStatus(ApartmentStatus.AVAILABLE);
        List<Apartment> matchingApartments = new ArrayList<>();

        for (Apartment apartment : availableApartments) {
            if (apartment.getAvailableFrom() != null
                    && apartment.getAvailableFrom().isBefore(LocalDate.now())) {
                continue;
            }
            if (minRent != null && apartment.getMonthlyRent() < minRent) {
                continue;
            }
            if (maxRent != null && apartment.getMonthlyRent() > maxRent) {
                continue;
            }
            if (bedrooms != null && !apartment.getBedrooms().equals(bedrooms)) {
                continue;
            }
            if (district != null && !district.isBlank()
                    && !apartment.getBuilding().getDistrict().equalsIgnoreCase(district.trim())) {
                continue;
            }
            if (isFurnished != null && !isFurnished.equals(apartment.getFurnished())) {
                continue;
            }
            matchingApartments.add(apartment);
        }

        if (matchingApartments.isEmpty()) {
            throw new ApiException("No apartments were found matching your search criteria!");
        }

        List<ApartmentDTOOut> apartmentDTOOuts = new ArrayList<>();
        for (Apartment apartment : matchingApartments) {
            apartmentDTOOuts.add(convertToDTO(apartment));
        }

        return apartmentDTOOuts;

    }
}
