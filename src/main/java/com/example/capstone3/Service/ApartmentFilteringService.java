package com.example.capstone3.Service;

import com.example.capstone3.Enums.ApartmentStatus;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.UserPreference;
import com.example.capstone3.Repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentFilteringService {

    private final ApartmentRepository apartmentRepository;

    public List<Apartment> filterByPreferences(UserPreference preferences) {

        List<Apartment> allAvailable = apartmentRepository.findByStatus(ApartmentStatus.AVAILABLE);
        List<Apartment> filtered = new ArrayList<>();

        for (Apartment apartment : allAvailable) {

            // Filter by budget
            if (apartment.getMonthlyRent() > preferences.getMaxBudget()) {
                continue;
            }

            // Filter by preferred bedrooms
            if (preferences.getPreferredBedrooms() != null) {
                if (apartment.getBedrooms() == null || apartment.getBedrooms() < preferences.getPreferredBedrooms()) {
                    continue;
                }
            }

            // Filter by preferred bathrooms
            if (preferences.getPreferredBathrooms() != null) {
                if (apartment.getBathrooms() == null || apartment.getBathrooms() < preferences.getPreferredBathrooms()) {
                    continue;
                }
            }

            // Filter by parking requirement
            if (Boolean.TRUE.equals(preferences.getRequiresParking())) {
                if (!Boolean.TRUE.equals(apartment.getBuilding().getHasParking())) {
                    continue;
                }
            }

            // Filter by elevator requirement
            if (Boolean.TRUE.equals(preferences.getRequiresElevator())) {
                if (!Boolean.TRUE.equals(apartment.getBuilding().getHasElevator())) {
                    continue;
                }
            }

            // Filter by furnished requirement
            if (Boolean.TRUE.equals(preferences.getRequiresFurnished())) {
                if (!Boolean.TRUE.equals(apartment.getFurnished())) {
                    continue;
                }
            }

            filtered.add(apartment);
        }

        return filtered;
    }
}
