package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.ApartmentComparisonDTOOut;
import com.example.capstone3.DTO.Out.ApartmentReviewSummaryDTOOut;
import com.example.capstone3.DTO.Out.ApartmentServicesDTOOut;
import com.example.capstone3.DTO.Out.NeighborhoodSummaryDTOOut;
import com.example.capstone3.DTO.Out.OwnerReputationSummaryDTOOut;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.Review;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiApartmentService {

    // Backend loads trusted facts; OpenAI only writes summaries and comparisons.
    private final ApartmentRepository apartmentRepository;
    private final ReviewRepository reviewRepository;
    private final OwnerRepository ownerRepository;
    private final OverpassLocationService overpassLocationService;
    private final AiService aiService;

    // Summarizes tenant reviews for one apartment using stored apartment facts.
    public ApartmentReviewSummaryDTOOut getReviewSummary(Integer apartmentId, String language) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }

        List<Review> reviews = reviewRepository.findReviewByApartmentId(apartmentId);
        if (reviews.isEmpty()) {
            throw new ApiException("No reviews found for this apartment");
        }

        String aiResponse = aiService.generateText(buildReviewSummaryPrompt(apartment, reviews), language);

        ApartmentReviewSummaryDTOOut response = new ApartmentReviewSummaryDTOOut();
        response.setSummary(aiService.cleanAiText(aiResponse));

        return response;
    }

    // Sends apartment details, ratings, and comments to OpenAI for summarization.
    private String buildReviewSummaryPrompt(Apartment apartment, List<Review> reviews) {
        StringBuilder prompt = new StringBuilder();

        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        prompt.append("You are a real estate analyst reviewing tenant feedback for a rental apartment in Saudi Arabia.\n\n");

        prompt.append("=== APARTMENT ===\n");
        prompt.append("Apartment Number: ").append(apartment.getApartmentNumber()).append("\n");
        prompt.append("District: ").append(apartment.getBuilding().getDistrict()).append("\n");
        prompt.append("City: ").append(apartment.getBuilding().getCity()).append("\n");
        prompt.append("Bedrooms: ").append(apartment.getBedrooms()).append("\n");
        prompt.append("Bathrooms: ").append(apartment.getBathrooms()).append("\n");
        prompt.append("Area: ").append(apartment.getArea()).append(" sqm\n");
        prompt.append("Furnished: ").append(Boolean.TRUE.equals(apartment.getFurnished()) ? "Yes" : "No").append("\n");
        prompt.append("Monthly Rent: SAR ").append(apartment.getMonthlyRent()).append("\n");
        prompt.append("Floor: ").append(apartment.getFloorNumber() != null ? apartment.getFloorNumber() : "Not specified").append("\n");
        prompt.append("Allowed Tenant Type: ").append(apartment.getAllowedTenantType() != null ? apartment.getAllowedTenantType() : "All").append("\n");
        prompt.append("Water Included: ").append(Boolean.TRUE.equals(apartment.getWaterIncluded()) ? "Yes" : "No").append("\n");
        prompt.append("Internet Included: ").append(Boolean.TRUE.equals(apartment.getInternetIncluded()) ? "Yes" : "No").append("\n");
        prompt.append("Electricity Included: ").append(Boolean.TRUE.equals(apartment.getElectricityIncluded()) ? "Yes" : "No").append("\n");

        prompt.append("\n=== BUILDING ===\n");
        prompt.append("Building Name: ").append(apartment.getBuilding().getName()).append("\n");
        prompt.append("Parking: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasParking()) ? "Yes" : "No").append("\n");
        prompt.append("Elevator: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasElevator()) ? "Yes" : "No").append("\n");
        prompt.append("Security: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasSecurity()) ? "Yes" : "No").append("\n");
        prompt.append("Pets Allowed: ").append(Boolean.TRUE.equals(apartment.getBuilding().getPetsAllowed()) ? "Yes" : "No").append("\n");

        prompt.append("\n=== REVIEW SUMMARY ===\n");
        prompt.append("Total Reviews: ").append(reviews.size()).append("\n");
        prompt.append("Average Rating: ").append(String.format("%.1f", avgRating)).append("/5\n");

        prompt.append("\n=== TENANT REVIEWS ===\n");
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
            prompt.append("\nReview ").append(i + 1).append(":\n");
            prompt.append("Rating: ").append(r.getRating()).append("/5\n");
            prompt.append("Comment: ").append(r.getComment()).append("\n");
        }

        prompt.append("\n=== OUTPUT INSTRUCTIONS ===\n");
        prompt.append("Do not write greetings or introductions. Start directly with the summary.\n");
        prompt.append("Provide a structured analysis covering:\n");
        prompt.append("1. Overall tenant satisfaction\n");
        prompt.append("2. Common strengths\n");
        prompt.append("3. Common complaints\n");
        prompt.append("4. Overall conclusion\n");
        prompt.append("Use concise professional language.");

        return prompt.toString();
    }

    // ─── Neighborhood Summary ─────────────────────────────────────────────────

    // Combines apartment data with nearby services for a neighborhood summary.
    public NeighborhoodSummaryDTOOut getNeighborhoodSummary(Integer apartmentId, String language) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }

        ApartmentServicesDTOOut amenities = overpassLocationService.analyzeApartmentLocation(
                apartment.getBuilding().getLatitude(),
                apartment.getBuilding().getLongitude(),
                3000
        );

        String aiResponse = aiService.generateText(buildNeighborhoodPrompt(apartment, amenities), language);

        NeighborhoodSummaryDTOOut.NearbyCounts counts = new NeighborhoodSummaryDTOOut.NearbyCounts();
        counts.setSchools(amenities.getSchoolCount());
        counts.setSupermarkets(amenities.getSupermarketCount());
        counts.setRestaurants(amenities.getRestaurantCount());
        counts.setHospitals(amenities.getHospitalCount());

        NeighborhoodSummaryDTOOut response = new NeighborhoodSummaryDTOOut();
        response.setDistrict(apartment.getBuilding().getDistrict());
        response.setNearbyCounts(counts);
        response.setSummary(aiService.cleanAiText(aiResponse));

        return response;
    }

    // Gives OpenAI location, building, and Overpass service data to describe.
    private String buildNeighborhoodPrompt(Apartment apartment, ApartmentServicesDTOOut amenities) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a real estate analyst describing the neighborhood of a rental apartment in Saudi Arabia.\n\n");

        prompt.append("=== APARTMENT LOCATION ===\n");
        prompt.append("Apartment Number: ").append(apartment.getApartmentNumber()).append("\n");
        prompt.append("District: ").append(apartment.getBuilding().getDistrict()).append("\n");
        prompt.append("City: ").append(apartment.getBuilding().getCity()).append("\n");
        prompt.append("Street: ").append(apartment.getBuilding().getStreet()).append("\n");
        prompt.append("Building Name: ").append(apartment.getBuilding().getName()).append("\n");

        prompt.append("\n=== APARTMENT FEATURES ===\n");
        prompt.append("Bedrooms: ").append(apartment.getBedrooms()).append("\n");
        prompt.append("Furnished: ").append(Boolean.TRUE.equals(apartment.getFurnished()) ? "Yes" : "No").append("\n");
        prompt.append("Allowed Tenant Type: ").append(apartment.getAllowedTenantType() != null ? apartment.getAllowedTenantType() : "All").append("\n");

        prompt.append("\n=== BUILDING FEATURES ===\n");
        prompt.append("Parking: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasParking()) ? "Yes" : "No").append("\n");
        prompt.append("Elevator: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasElevator()) ? "Yes" : "No").append("\n");
        prompt.append("Security: ").append(Boolean.TRUE.equals(apartment.getBuilding().getHasSecurity()) ? "Yes" : "No").append("\n");
        prompt.append("Pets Allowed: ").append(Boolean.TRUE.equals(apartment.getBuilding().getPetsAllowed()) ? "Yes" : "No").append("\n");
        if (apartment.getBuilding().getConstructionYear() != null) {
            prompt.append("Construction Year: ").append(apartment.getBuilding().getConstructionYear()).append("\n");
        }
        if (apartment.getBuilding().getTotalFloors() != null) {
            prompt.append("Total Floors: ").append(apartment.getBuilding().getTotalFloors()).append("\n");
        }

        prompt.append("\n=== NEARBY SERVICES (within 3000m) ===\n");
        prompt.append("Hospitals: ").append(amenities.getHospitalCount()).append("\n");
        prompt.append("Schools: ").append(amenities.getSchoolCount()).append("\n");
        prompt.append("Supermarkets: ").append(amenities.getSupermarketCount()).append("\n");
        prompt.append("Pharmacies: ").append(amenities.getPharmacyCount()).append("\n");
        prompt.append("Gyms: ").append(amenities.getGymCount()).append("\n");
        prompt.append("Restaurants: ").append(amenities.getRestaurantCount()).append("\n");

        prompt.append("\n=== OUTPUT INSTRUCTIONS ===\n");
        prompt.append("Do not write greetings or introductions. Start directly with the neighborhood description.\n");
        prompt.append("Describe the neighborhood covering:\n");
        prompt.append("1. Family suitability\n");
        prompt.append("2. Daily convenience\n");
        prompt.append("3. Access to services\n");
        prompt.append("4. Overall neighborhood quality\n");
        prompt.append("Use concise professional language.");

        return prompt.toString();
    }


    // Summarizes an owner's reputation from apartments and tenant reviews.
    public OwnerReputationSummaryDTOOut getOwnerReputationSummary(Integer ownerId, String language) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }

        List<Apartment> apartments = apartmentRepository.findApartmentsByOwnerId(ownerId);
        if (apartments.isEmpty()) {
            throw new ApiException("No apartments found for this owner");
        }

        List<Review> reviews = reviewRepository.findByApartment_Building_Owner_Id(ownerId);
        if (reviews.isEmpty()) {
            throw new ApiException("No reviews found for this owner's apartments");
        }

        String aiResponse = aiService.generateText(buildOwnerReputationPrompt(owner, apartments, reviews), language);

        OwnerReputationSummaryDTOOut response = new OwnerReputationSummaryDTOOut();
        response.setSummary(aiService.cleanAiText(aiResponse));

        return response;
    }

    // Gives OpenAI owner facts and review evidence for reputation analysis.
    private String buildOwnerReputationPrompt(Owner owner, List<Apartment> apartments, List<Review> reviews) {
        StringBuilder prompt = new StringBuilder();

        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        prompt.append("You are a real estate analyst evaluating an apartment owner's reputation in Saudi Arabia.\n\n");

        prompt.append("=== OWNER INFORMATION ===\n");
        prompt.append("Name: ").append(owner.getFullName()).append("\n");
        prompt.append("Total Apartments: ").append(apartments.size()).append("\n");
        prompt.append("Total Reviews Received: ").append(reviews.size()).append("\n");
        prompt.append("Overall Average Rating: ").append(String.format("%.1f", avgRating)).append("/5\n");

        prompt.append("\n=== APARTMENTS OWNED ===\n");
        for (Apartment apt : apartments) {
            List<Review> aptReviews = reviews.stream()
                    .filter(r -> r.getApartment().getId().equals(apt.getId()))
                    .toList();
            double aptAvg = aptReviews.stream().mapToInt(Review::getRating).average().orElse(0);

            prompt.append("\n- Apartment ").append(apt.getApartmentNumber()).append("\n");
            prompt.append("  District: ").append(apt.getBuilding().getDistrict())
                    .append(", ").append(apt.getBuilding().getCity()).append("\n");
            prompt.append("  Bedrooms: ").append(apt.getBedrooms())
                    .append(" | Bathrooms: ").append(apt.getBathrooms())
                    .append(" | Area: ").append(apt.getArea()).append(" sqm\n");
            prompt.append("  Furnished: ").append(Boolean.TRUE.equals(apt.getFurnished()) ? "Yes" : "No")
                    .append(" | Monthly Rent: SAR ").append(apt.getMonthlyRent()).append("\n");
            prompt.append("  Parking: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasParking()) ? "Yes" : "No")
                    .append(" | Elevator: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasElevator()) ? "Yes" : "No")
                    .append(" | Security: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasSecurity()) ? "Yes" : "No").append("\n");
            if (!aptReviews.isEmpty()) {
                prompt.append("  Reviews: ").append(aptReviews.size())
                        .append(" | Avg Rating: ").append(String.format("%.1f", aptAvg)).append("/5\n");
            } else {
                prompt.append("  Reviews: None\n");
            }
        }

        prompt.append("\n=== ALL TENANT REVIEWS ===\n");
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
            prompt.append("\nReview ").append(i + 1).append(" (Apartment ")
                    .append(r.getApartment().getApartmentNumber()).append("):\n");
            prompt.append("Rating: ").append(r.getRating()).append("/5\n");
            prompt.append("Comment: ").append(r.getComment()).append("\n");
        }

        prompt.append("\n=== OUTPUT INSTRUCTIONS ===\n");
        prompt.append("Do not write greetings or introductions. Start directly with the reputation analysis.\n");
        prompt.append("Provide a structured analysis covering:\n");
        prompt.append("1. Overall reputation\n");
        prompt.append("2. Common strengths\n");
        prompt.append("3. Common complaints\n");
        prompt.append("4. Communication quality (based on review comments)\n");
        prompt.append("5. Maintenance responsiveness (if mentioned in reviews)\n");
        prompt.append("6. Overall conclusion\n");
        prompt.append("Use concise professional language.");

        return prompt.toString();
    }


    // Get the selected apartments before OpenAI compares their supplied facts.
    public ApartmentComparisonDTOOut compareApartments(List<Integer> apartmentIds, String language) {
        if (apartmentIds == null || apartmentIds.size() < 2 || apartmentIds.size() > 3) {
            throw new ApiException("Please provide 2 or 3 apartment IDs to compare");
        }
        if (apartmentIds.stream().distinct().count() != apartmentIds.size()) {
            throw new ApiException("An apartment cannot be compared with itself");
        }

        List<Apartment> apartments = new ArrayList<>();
        for (Integer id : apartmentIds) {
            Apartment apt = apartmentRepository.findApartmentById(id);
            if (apt == null) {
                throw new ApiException("Apartment not found with ID: " + id);
            }
            apartments.add(apt);
        }

        String aiResponse = aiService.generateText(buildComparisonPrompt(apartments), language);

        ApartmentComparisonDTOOut response = new ApartmentComparisonDTOOut();
        response.setComparison(normalizeComparisonText(aiResponse));

        return response;
    }

    // Limits OpenAI to the apartment, owner, review, and service data provided.
    private String buildComparisonPrompt(List<Apartment> apartments) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a real estate analyst comparing rental apartments in Saudi Arabia.\n\n");

        for (int i = 0; i < apartments.size(); i++) {
            Apartment apt = apartments.get(i);
            Owner owner = apt.getBuilding().getOwner();
            List<Review> reviews = reviewRepository.findReviewByApartmentId(apt.getId());

            int ratingSum = 0;
            for (Review r : reviews) {
                ratingSum += r.getRating();
            }
            double avgRating = reviews.isEmpty() ? 0 : (double) ratingSum / reviews.size();

            prompt.append("Apartment ").append(i + 1).append(" Number: ")
                    .append(apt.getApartmentNumber()).append("\n");
            prompt.append("District: ").append(apt.getBuilding().getDistrict()).append("\n");
            prompt.append("City: ").append(apt.getBuilding().getCity()).append("\n");
            prompt.append("Building: ").append(apt.getBuilding().getName()).append("\n");
            prompt.append("Monthly Rent: SAR ").append(apt.getMonthlyRent()).append("\n");
            prompt.append("Area: ").append(apt.getArea()).append(" sqm\n");
            prompt.append("Bedrooms: ").append(apt.getBedrooms()).append("\n");
            prompt.append("Bathrooms: ").append(apt.getBathrooms()).append("\n");
            prompt.append("Furnished: ").append(Boolean.TRUE.equals(apt.getFurnished()) ? "Yes" : "No").append("\n");
            prompt.append("Floor: ").append(apt.getFloorNumber() != null ? apt.getFloorNumber() : "Not specified").append("\n");
            prompt.append("Allowed Tenant Type: ").append(apt.getAllowedTenantType() != null ? apt.getAllowedTenantType() : "All").append("\n");
            prompt.append("Water Included: ").append(Boolean.TRUE.equals(apt.getWaterIncluded()) ? "Yes" : "No").append("\n");
            prompt.append("Internet Included: ").append(Boolean.TRUE.equals(apt.getInternetIncluded()) ? "Yes" : "No").append("\n");
            prompt.append("Electricity Included: ").append(Boolean.TRUE.equals(apt.getElectricityIncluded()) ? "Yes" : "No").append("\n");
            prompt.append("Parking: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasParking()) ? "Yes" : "No").append("\n");
            prompt.append("Elevator: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasElevator()) ? "Yes" : "No").append("\n");
            prompt.append("Security: ").append(Boolean.TRUE.equals(apt.getBuilding().getHasSecurity()) ? "Yes" : "No").append("\n");
            prompt.append("Pets Allowed: ").append(Boolean.TRUE.equals(apt.getBuilding().getPetsAllowed()) ? "Yes" : "No").append("\n");
            if (apt.getBuilding().getConstructionYear() != null) {
                prompt.append("Construction Year: ").append(apt.getBuilding().getConstructionYear()).append("\n");
            }
            if (apt.getBuilding().getTotalFloors() != null) {
                prompt.append("Total Floors in Building: ").append(apt.getBuilding().getTotalFloors()).append("\n");
            }
            // Owner reputation
            List<Review> ownerReviews = reviewRepository.findByApartment_Building_Owner_Id(owner.getId());
            List<Apartment> ownerApartments = apartmentRepository.findApartmentsByOwnerId(owner.getId());
            int ownerRatingSum = 0;
            for (Review r : ownerReviews) {
                ownerRatingSum += r.getRating();
            }
            double ownerAvgRating = ownerReviews.isEmpty() ? 0 : (double) ownerRatingSum / ownerReviews.size();
            prompt.append("Owner Name: ").append(owner.getFullName()).append("\n");
            prompt.append("Owner Total Apartments: ").append(ownerApartments.size()).append("\n");
            prompt.append("Owner Total Reviews: ").append(ownerReviews.size()).append("\n");
            if (!ownerReviews.isEmpty()) {
                prompt.append("Owner Average Rating: ").append(String.format("%.1f", ownerAvgRating)).append("/5\n");
            }

            // Reviews (capped at 10)
            if (!reviews.isEmpty()) {
                prompt.append("Average Rating: ").append(String.format("%.1f", avgRating))
                        .append("/5 (").append(reviews.size()).append(" reviews)\n");
                prompt.append("Review Comments (up to 10):\n");
                int count = 0;
                for (Review r : reviews) {
                    if (count >= 10) break;
                    prompt.append("  - [").append(r.getRating()).append("/5] ").append(r.getComment()).append("\n");
                    count++;
                }
            } else {
                prompt.append("Reviews: No reviews yet\n");
            }

            // Nearby services
            try {
                ApartmentServicesDTOOut amenities = overpassLocationService.analyzeApartmentLocation(
                        apt.getBuilding().getLatitude(),
                        apt.getBuilding().getLongitude(),
                        3000
                );
                prompt.append("Nearby Services (within 3000m): ");
                prompt.append("Hospitals: ").append(amenities.getHospitalCount()).append(", ");
                prompt.append("Schools: ").append(amenities.getSchoolCount()).append(", ");
                prompt.append("Supermarkets: ").append(amenities.getSupermarketCount()).append(", ");
                prompt.append("Pharmacies: ").append(amenities.getPharmacyCount()).append(", ");
                prompt.append("Gyms: ").append(amenities.getGymCount()).append(", ");
                prompt.append("Restaurants: ").append(amenities.getRestaurantCount()).append("\n");
            } catch (Exception e) {
                prompt.append("Nearby Services: Data unavailable\n");
            }

            prompt.append("\n");
        }

        prompt.append("Output instructions\n");
        prompt.append("Return one clean plain-text comparison block.\n");
        prompt.append("Do not use Markdown, headings, bold text, hash symbols, or bullet lists.\n");
        prompt.append("Do not mention apartment IDs, backend scores, or ranking logic.\n");
        prompt.append("Use one short paragraph for each apartment and one line for each conclusion.\n");
        prompt.append("Use this exact style:\n");
        for (int i = 0; i < apartments.size(); i++) {
            prompt.append("Apartment ").append(i + 1)
                    .append(": Summarize the main strengths in one sentence. Weaknesses: summarize the main weaknesses.\n");
        }
        prompt.append("Best for families: State which apartment fits families best and why.\n");
        prompt.append("Best value: State which apartment offers the best value and why.\n");
        prompt.append("Final recommendation: State which renter type each apartment suits best.\n");
        prompt.append("Base every conclusion strictly on the apartment data provided above.\n");
        prompt.append("Use concise professional language with no repeated blank lines.");

        return prompt.toString();
    }

    // Remove extra spacing so the comparison stays readable in JSON responses.
    private String normalizeComparisonText(String comparison) {
        if (comparison == null) return "";
        return aiService.cleanAiText(comparison);
    }
}
