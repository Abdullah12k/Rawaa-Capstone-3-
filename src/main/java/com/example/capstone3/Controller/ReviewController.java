package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.ReviewDTOIn;
import com.example.capstone3.Service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/get")
    public ResponseEntity<?> getReviews() {
        return ResponseEntity.status(200).body(reviewService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReview(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(reviewService.getReview(id));
    }

    @PostMapping("/add/{userId}/{reservationId}")
    public ResponseEntity<?> addReview(@PathVariable Integer userId,
                                       @PathVariable Integer reservationId,
                                       @RequestBody @Valid ReviewDTOIn dto) {
        reviewService.addReview(userId, reservationId, dto);
        return ResponseEntity.status(200).body(new ApiResponse("Review added successfully"));
    }

    @PutMapping("/update/{userId}/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Integer userId,
                                          @PathVariable Integer id,
                                          @RequestBody @Valid ReviewDTOIn dto) {
        reviewService.updateReview(userId, id, dto);
        return ResponseEntity.status(200).body(new ApiResponse("Review updated successfully"));
    }

    @DeleteMapping("/delete/{userId}/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer userId, @PathVariable Integer id) {
        reviewService.deleteReview(userId, id);
        return ResponseEntity.status(200).body(new ApiResponse("Review deleted successfully"));
    }

    @GetMapping("/get/apartment/{apartmentId}")
    public ResponseEntity<?> getReviewsByApartment(@PathVariable Integer apartmentId) {
        return ResponseEntity.status(200).body(reviewService.getReviewByApartment(apartmentId));
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(reviewService.getReviewsByUserId(userId));
    }

    @GetMapping("/get/owner/{ownerId}")
    public ResponseEntity<?> getOwnerReviews(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(reviewService.getOwnerReviews(ownerId));
    }

    // Generates an AI analysis of reviews across an owner's apartments.
    @GetMapping("/get/owner-analysis/{ownerId}")
    public ResponseEntity<?> getOwnerReviewAnalysis(@PathVariable Integer ownerId,
                                                    @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(reviewService.generateOwnerAnalysis(ownerId, language));
    }
}
