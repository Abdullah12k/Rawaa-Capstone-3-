package com.example.capstone3.Controller;

import com.example.capstone3.Service.AiApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiApartmentController {

    private final AiApartmentService aiApartmentService;

    // Analyzes apartment reviews using AI.
    @GetMapping("/apartments/review-summary/{apartmentId}")
    public ResponseEntity<?> getReviewSummary(@PathVariable Integer apartmentId, @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(aiApartmentService.getReviewSummary(apartmentId, language));
    }

    // Describes an apartment neighborhood using AI and nearby service data.
    @GetMapping("/apartments/neighborhood-summary/{apartmentId}")
    public ResponseEntity<?> getNeighborhoodSummary(@PathVariable Integer apartmentId, @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(aiApartmentService.getNeighborhoodSummary(apartmentId, language));
    }

    // Generates an AI summary of owner reputation from tenant reviews.
    @GetMapping("/owners/reputation-summary/{ownerId}")
    public ResponseEntity<?> getOwnerReputationSummary(@PathVariable Integer ownerId, @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(aiApartmentService.getOwnerReputationSummary(ownerId, language));
    }

    // Compares the supplied apartments using AI.
    @GetMapping("/apartments/compare/{id1}/{id2}")
    public ResponseEntity<?> compareTwoApartments(@PathVariable Integer id1, @PathVariable Integer id2, @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(aiApartmentService.compareApartments(List.of(id1, id2), language));
    }

}
