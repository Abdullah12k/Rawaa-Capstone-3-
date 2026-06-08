package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.ReviewDTOIn;
import com.example.capstone3.DTO.Out.OwnerReviewAnalysisDTOOut;
import com.example.capstone3.DTO.Out.ReviewDTOOut;
import com.example.capstone3.Enums.ReservationStatus;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.Reservation;
import com.example.capstone3.Models.Review;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.ReservationRepository;
import com.example.capstone3.Repository.ReviewRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final OwnerRepository ownerRepository;
    private final ReservationRepository reservationRepository;
    private final AiService aiService;
    private final WhatsAppService whatsAppService;

    public List<ReviewDTOOut> getAll() {
        List<ReviewDTOOut> reviewDTOOuts = new ArrayList<>();
        for (Review review : reviewRepository.findAll()) {
            reviewDTOOuts.add(convertToDTO(review));
        }
        return reviewDTOOuts;
    }

    public ReviewDTOOut getReview(Integer id) {
        Review review = reviewRepository.findReviewById(id);
        if (review == null) {
            throw new ApiException("Review not found");
        }
        return convertToDTO(review);
    }

    public void addReview(Integer userId, Integer reservationId, ReviewDTOIn dto) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        Reservation reservation = reservationRepository.findReservationById(reservationId);
        if (reservation == null) {
            throw new ApiException("Reservation not found");
        }
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ApiException("Reservation does not belong to this user");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ApiException("You can only review apartments from completed reservations");
        }
        if (reviewRepository.existsByReservation_Id(reservationId)) {
            throw new ApiException("You have already submitted a review for this reservation");
        }
        Review review = new Review();
        review.setUser(user);
        review.setApartment(reservation.getApartment());
        review.setReservation(reservation);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        reviewRepository.save(review);

        whatsAppService.notifyOwnerNewReview(reservation.getApartment().getOwner(), review.getApartment(), review);
    }

    public void updateReview(Integer userId, Integer id, ReviewDTOIn dto) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        Review review = reviewRepository.findReviewById(id);
        if (review == null) {
            throw new ApiException("Review not found");
        }
        if (!review.getUser().getId().equals(userId)) {
            throw new ApiException("You can only update your own review");
        }
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        reviewRepository.save(review);
    }

    public void deleteReview(Integer userId, Integer id) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        Review review = reviewRepository.findReviewById(id);
        if (review == null) {
            throw new ApiException("Review not found");
        }
        if (!review.getUser().getId().equals(userId)) {
            throw new ApiException("You can only delete your own review");
        }
        reviewRepository.deleteById(id);
    }


    //^^^^^^^CRUD^^^^^^^^


    public List<ReviewDTOOut> getReviewByApartment(Integer apartmentId) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        List<ReviewDTOOut> reviewDTOOuts = new ArrayList<>();
        for (Review review : reviewRepository.findReviewByApartmentId(apartmentId)) {
            reviewDTOOuts.add(convertToDTO(review));
        }
        return reviewDTOOuts;
    }

    public List<ReviewDTOOut> getReviewsByUserId(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        List<ReviewDTOOut> reviewDTOOuts = new ArrayList<>();
        for (Review review : reviewRepository.findReviewsByUserId(userId)) {
            reviewDTOOuts.add(convertToDTO(review));
        }
        return reviewDTOOuts;
    }

    public List<ReviewDTOOut> getOwnerReviews(Integer ownerId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        List<ReviewDTOOut> reviewDTOOuts = new ArrayList<>();
        for (Review review : reviewRepository.findByApartment_Building_Owner_Id(ownerId)) {
            reviewDTOOuts.add(convertToDTO(review));
        }
        return reviewDTOOuts;
    }

    // gets owner reviews, calculates backend statistics, and requests AI analysis.
    public OwnerReviewAnalysisDTOOut generateOwnerAnalysis(Integer ownerId, String language) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        List<Review> reviews =
                reviewRepository.findByApartment_Building_Owner_Id(ownerId);
        if (reviews.isEmpty()) {
            throw new ApiException("No reviews found for this owner");
        }
        // OpenAI writes the analysis; review count and average rating stay backend-calculated.
        String analysis = aiService.generateOwnerReviewAnalysis(owner, reviews, language);

        OwnerReviewAnalysisDTOOut dto = new OwnerReviewAnalysisDTOOut();
        dto.setAnalysis(aiService.cleanAiText(analysis));
        return dto;
    }


    public ReviewDTOOut convertToDTO(Review review) {
        ReviewDTOOut reviewDTOOut = new ReviewDTOOut();
        reviewDTOOut.setId(review.getId());
        reviewDTOOut.setUserId(review.getUser().getId());
        reviewDTOOut.setApartmentId(review.getApartment().getId());
        reviewDTOOut.setRating(review.getRating());
        reviewDTOOut.setComment(review.getComment());
        reviewDTOOut.setCreatedAt(review.getCreatedAt());
        return reviewDTOOut;
    }

}
