package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.ApartmentServicesDTOOut;
import com.example.capstone3.DTO.Out.CommuteAnalysisDTOOut;
import com.example.capstone3.DTO.Out.RankedApartmentDTOOut;
import com.example.capstone3.DTO.Out.RecommendationResponseDTOOut;
import com.example.capstone3.Enums.PreferenceLevel;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Review;
import com.example.capstone3.Models.User;
import com.example.capstone3.Models.UserPreference;
import com.example.capstone3.Repository.ReviewRepository;
import com.example.capstone3.Repository.UserPreferenceRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    // Filters, scores, and ranks apartments before OpenAI explains the result.
    private final ApartmentFilteringService filteringService;
    private final OverpassLocationService overpassLocationService;
    private final OsrmCommuteService osrmCommuteService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final AiService aiService;

    // Loads the user flow, calculates rankings, and asks AI for an explanation.
    public RecommendationResponseDTOOut recommend(Integer userId, int radiusMetres, String language) {
        if (radiusMetres < 100 || radiusMetres > 10000) {
            throw new ApiException("Recommendation radius must be between 100 and 10000 metres");
        }

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        UserPreference preferences = userPreferenceRepository.findUserPreferenceByUserId(userId);
        if (preferences == null) {
            throw new ApiException("User preferences not found. Please add your preferences first.");
        }

        List<Apartment> apartments = filteringService.filterByPreferences(preferences);
        if (apartments.isEmpty()) {
            throw new ApiException("No available apartments match your preferences.");
        }

        // The backend owns ranking so AI cannot change the scoring result.
        List<RankedApartmentDTOOut> rankedApartments = new ArrayList<>();
        // Score every apartment first so sorting compares complete results.
        for (Apartment apartment : apartments) {
            rankedApartments.add(scoreApartment(apartment, preferences, user, radiusMetres));
        }

        // Highest score wins; lower rent and ID provide stable tie-breakers.
        rankedApartments.sort(Comparator
                .comparingDouble((RankedApartmentDTOOut apartment) -> apartment.getTotalScore())
                .reversed()
                .thenComparing(apartment -> apartment.getMonthlyRent())
                .thenComparing(apartment -> apartment.getApartmentId()));

        // Only the backend-selected top matches are sent to AI.
        List<RankedApartmentDTOOut> topMatches = rankedApartments.stream().limit(3).toList();
        // Assign display ranks after selecting the top matches.
        for (int i = 0; i < topMatches.size(); i++) {
            topMatches.get(i).setRank(i + 1);
        }

        RecommendationResponseDTOOut response = new RecommendationResponseDTOOut();
        response.setRankedApartments(topMatches);
        // AI explains the final ranking but does not choose apartments.
        String aiResult = aiService.generateText(buildExplanationPrompt(topMatches, preferences, user), language);
        response.setRecommendation(aiService.cleanAiText(aiResult));
        return response;
    }

    // Collect apartment facts and calculate every backend score category.
    private RankedApartmentDTOOut scoreApartment(Apartment apartment, UserPreference preferences,
                                                  User user, int radiusMetres) {
        ApartmentServicesDTOOut nearbyAmenities = getNearbyAmenities(apartment, radiusMetres);
        CommuteAnalysisDTOOut commuteAnalysis = getCommuteAnalysis(apartment, preferences);
        List<Review> reviews = reviewRepository.findReviewByApartmentId(apartment.getId());
        double averageRating = reviews.stream()
                .mapToInt(review -> review.getRating())
                .average()
                .orElse(0);

        double budgetScore = calculateBudgetScore(apartment, preferences);
        double amenityScore = calculateAmenityScore(nearbyAmenities, preferences);
        double commuteScore = calculateCommuteScore(commuteAnalysis);
        double familyScore = calculateFamilyScore(apartment, nearbyAmenities, user);
        double apartmentScore = calculateApartmentScore(apartment, preferences, averageRating);
        double totalScore = budgetScore + amenityScore + commuteScore + familyScore + apartmentScore;

        RankedApartmentDTOOut result = new RankedApartmentDTOOut();
        result.setApartmentId(apartment.getId());
        result.setApartmentNumber(apartment.getApartmentNumber());
        result.setDistrict(apartment.getBuilding().getDistrict());
        result.setMonthlyRent(apartment.getMonthlyRent());
        result.setBedrooms(apartment.getBedrooms());
        result.setBathrooms(apartment.getBathrooms());
        result.setArea(apartment.getArea());
        result.setFurnished(apartment.getFurnished());
        result.setWaterIncluded(apartment.getWaterIncluded());
        result.setElectricityIncluded(apartment.getElectricityIncluded());
        result.setInternetIncluded(apartment.getInternetIncluded());
        result.setBuildingHasParking(apartment.getBuilding().getHasParking());
        result.setBuildingHasElevator(apartment.getBuilding().getHasElevator());
        result.setBuildingHasSecurity(apartment.getBuilding().getHasSecurity());
        result.setBudgetScore(round(budgetScore));
        result.setAmenityScore(round(amenityScore));
        result.setCommuteScore(round(commuteScore));
        result.setFamilyScore(round(familyScore));
        result.setApartmentScore(round(apartmentScore));
        result.setTotalScore(round(totalScore));
        result.setAverageRating(round(averageRating));
        result.setCommuteMinutes(commuteAnalysis == null ? null : commuteAnalysis.getDurationMinutes());
        result.setCommuteDistanceKm(commuteAnalysis == null ? null : round(commuteAnalysis.getDistanceKm()));
        result.setNearbyServices(nearbyAmenities);
        return result;
    }

    // Overpass returns nearby services used by amenity and family scoring.
    private ApartmentServicesDTOOut getNearbyAmenities(Apartment apartment, int radiusMetres) {
        try {
            return overpassLocationService.analyzeApartmentLocation(
                    apartment.getBuilding().getLatitude(),
                    apartment.getBuilding().getLongitude(),
                    radiusMetres);
        } catch (Exception ignored) {
            return new ApartmentServicesDTOOut();
        }
    }

    // OSRM returns commute duration and distance from the apartment to work.
    private CommuteAnalysisDTOOut getCommuteAnalysis(Apartment apartment, UserPreference preferences) {
        if (preferences.getWorkLatitude() == null || preferences.getWorkLongitude() == null) {
            return null;
        }
        try {
            return osrmCommuteService.analyzeCommute(
                    apartment.getBuilding().getLatitude(),
                    apartment.getBuilding().getLongitude(),
                    preferences.getWorkLatitude(),
                    preferences.getWorkLongitude());
        } catch (Exception ignored) {
            return null;
        }
    }

    private double calculateBudgetScore(Apartment apartment, UserPreference preferences) {
        // Reward apartments within budget, with more room below the limit scoring higher.
        double budget = preferences.getMaxBudget();
        double remainingRatio = Math.max(0, (budget - apartment.getMonthlyRent()) / budget);
        return 18 + (remainingRatio * 7);
    }

    private double calculateAmenityScore(ApartmentServicesDTOOut amenities, UserPreference preferences) {
        // Weight nearby services by the user's amenity preferences.
        double rawScore =
                amenities.getHospitalCount() * 4 * preferenceMultiplier(preferences.getHospitalPreference())
                        + amenities.getSchoolCount() * 5 * preferenceMultiplier(preferences.getSchoolPreference())
                        + amenities.getSupermarketCount() * 3
                        + amenities.getPharmacyCount() * 4
                        + amenities.getGymCount() * 2 * preferenceMultiplier(preferences.getGymPreference())
                        + amenities.getRestaurantCount()
                        * preferenceMultiplier(preferences.getCafesPreference());
        return Math.min(20, rawScore / 4);
    }

    private double calculateCommuteScore(CommuteAnalysisDTOOut commute) {
        // OSRM commute duration is unavailable when route analysis fails.
        if (commute == null) return 0;

        // Shorter OSRM duration and distance receive a higher commute score.
        double durationPenalty = commute.getDurationMinutes() * 0.2;
        double distancePenalty = commute.getDistanceKm() * 0.1;
        return Math.max(0, 15 - durationPenalty - distancePenalty);
    }

    private double calculateFamilyScore(Apartment apartment, ApartmentServicesDTOOut amenities, User user) {
        double score = 0;
        int childrenCount = user.getChildrenCount() == null ? 0 : user.getChildrenCount();
        // Use familyCount when provided; otherwise preserve the children-based estimate.
        int householdSize = user.getFamilyCount() == null ? 1 + childrenCount : user.getFamilyCount();
        // Family size and children identify family-oriented renters.
        boolean familyRenter = childrenCount > 0
                || (user.getFamilyCount() != null && user.getFamilyCount() > 1);

        // Reward enough bedrooms for the estimated household size.
        if (apartment.getBedrooms() >= Math.max(1, Math.ceil(householdSize / 2.0))) score += 6;

        // Apply extra points based on whether the renter is family-oriented.
        if (familyRenter) {
            score += Math.min(5, amenities.getSchoolCount());
            if (Boolean.TRUE.equals(apartment.getBuilding().getHasParking())) score += 2;
            if (Boolean.TRUE.equals(apartment.getBuilding().getHasSecurity())) score += 2;
        } else {
            score += Math.min(5, amenities.getGymCount() + amenities.getRestaurantCount() / 2.0);
            score += 4;
        }
        if (matchesAllowedTenantType(apartment.getAllowedTenantType(), familyRenter)) {
            score += 1;
        }
        return Math.min(15, score);
    }

    private boolean matchesAllowedTenantType(String allowedTenantType, boolean familyRenter) {
        if (allowedTenantType == null || allowedTenantType.isBlank()) {
            return false;
        }
        String normalizedType = allowedTenantType.toLowerCase();
        return familyRenter
                ? normalizedType.contains("family")
                : normalizedType.contains("single") || normalizedType.contains("bachelor");
    }

    private double calculateApartmentScore(Apartment apartment, UserPreference preferences,
                                           double averageRating) {
        // Measure apartment fit using preferences, features, and review rating.
        double score = 0;
        if (preferences.getPreferredDistrict() != null
                && preferences.getPreferredDistrict().equalsIgnoreCase(apartment.getBuilding().getDistrict())) {
            score += 5;
        }
        if (preferences.getPreferredBedrooms() == null
                || apartment.getBedrooms() >= preferences.getPreferredBedrooms()) score += 3;
        if (preferences.getPreferredBathrooms() == null
                || apartment.getBathrooms() >= preferences.getPreferredBathrooms()) score += 2;
        if (Boolean.TRUE.equals(apartment.getFurnished())) score += 1;
        if (Boolean.TRUE.equals(apartment.getBuilding().getHasElevator())) score += 1;
        if (Boolean.TRUE.equals(apartment.getBuilding().getHasParking())) score += 1;
        if (Boolean.TRUE.equals(apartment.getBuilding().getHasSecurity())) score += 2;
        if (Boolean.TRUE.equals(apartment.getWaterIncluded())) score += 1;
        if (Boolean.TRUE.equals(apartment.getInternetIncluded())) score += 1;
        if (Boolean.TRUE.equals(apartment.getElectricityIncluded())) score += 1;
        score += (averageRating / 5.0) * 7;
        return Math.min(25, score);
    }

    // Convert preference importance into the multiplier used by amenity scoring.
    private double preferenceMultiplier(PreferenceLevel level) {
        if (level == PreferenceLevel.VERY_IMPORTANT) return 1.5;
        if (level == PreferenceLevel.PREFERRED) return 1.2;
        return 1.0;
    }

    // Give OpenAI final ranked data so it explains rather than recalculates results.
    private String buildExplanationPrompt(List<RankedApartmentDTOOut> rankedApartments,
                                          UserPreference preferences, User user) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Explain this apartment ranking for a renter in Saudi Arabia.\n");
        prompt.append("The backend calculated the final order. Keep the exact order and only explain it.\n");
        prompt.append("Do not invent data, change the order, or recommend apartments outside this list.\n");
        prompt.append("Do not mention apartment IDs or internal score category names.\n");
        prompt.append("Return plain text only. Do not use Markdown headings, bold text, or hash symbols.\n\n");
        prompt.append("Family count: ").append(user.getFamilyCount()).append("\n");
        prompt.append("Children: ").append(user.getChildrenCount()).append("\n");
        prompt.append("Maximum budget: SAR ").append(preferences.getMaxBudget()).append("\n\n");

        for (RankedApartmentDTOOut apartment : rankedApartments) {
            prompt.append("Rank ").append(apartment.getRank()).append(": ")
                    .append("Apartment ").append(apartment.getApartmentNumber()).append("\n");
            prompt.append("District: ").append(apartment.getDistrict()).append("\n");
            prompt.append("Monthly rent: SAR ").append(apartment.getMonthlyRent()).append("\n");
            prompt.append("Bedrooms: ").append(apartment.getBedrooms()).append("\n");
            prompt.append("Bathrooms: ").append(apartment.getBathrooms()).append("\n");
            prompt.append("Area: ").append(apartment.getArea()).append(" sqm\n");
            prompt.append("Furnished: ").append(apartment.getFurnished()).append("\n");
            prompt.append("Water included: ").append(apartment.getWaterIncluded()).append("\n");
            prompt.append("Electricity included: ").append(apartment.getElectricityIncluded()).append("\n");
            prompt.append("Internet included: ").append(apartment.getInternetIncluded()).append("\n");
            prompt.append("Building parking: ").append(apartment.getBuildingHasParking()).append("\n");
            prompt.append("Building elevator: ").append(apartment.getBuildingHasElevator()).append("\n");
            prompt.append("Building security: ").append(apartment.getBuildingHasSecurity()).append("\n");
            prompt.append("Overall backend score: ").append(apartment.getTotalScore()).append("/100\n");
            prompt.append("Budget fit points: ").append(apartment.getBudgetScore()).append("/25\n");
            prompt.append("Nearby service points: ").append(apartment.getAmenityScore()).append("/20\n");
            prompt.append("Travel convenience points: ").append(apartment.getCommuteScore()).append("/15\n");
            prompt.append("Household suitability points: ").append(apartment.getFamilyScore()).append("/15\n");
            prompt.append("Apartment feature and review points: ").append(apartment.getApartmentScore()).append("/25\n");
            prompt.append("Commute minutes: ")
                    .append(apartment.getCommuteMinutes() == null ? "Unavailable" : apartment.getCommuteMinutes())
                    .append("\n");
            prompt.append("Commute distance: ")
                    .append(apartment.getCommuteDistanceKm() == null
                            ? "Unavailable" : apartment.getCommuteDistanceKm() + " km")
                    .append("\n\n");
        }

        prompt.append("Use this plain-text format:\n\n");
        prompt.append("Top Matches\n\n");
        prompt.append("1. Apartment number\n");
        prompt.append("A short user-friendly explanation.\n\n");
        prompt.append("Repeat for each supplied apartment in the exact rank order.\n\n");
        prompt.append("Why the first apartment ranked highest:\n");
        prompt.append("A short explanation of its overall balance.\n\n");
        prompt.append("Use only the supplied facts. Do not show internal point values in the response.");
        return prompt.toString();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
