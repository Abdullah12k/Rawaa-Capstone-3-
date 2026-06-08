package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.UserPreferenceDTOIn;
import com.example.capstone3.DTO.In.WorkplaceDTOIn;
import com.example.capstone3.Service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/user-preference", "/api/v1/user-preferences"})
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping("/get")
    public ResponseEntity<?> getUserPreferences() {
        return ResponseEntity.status(200).body(userPreferenceService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUserPreference(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(userPreferenceService.getUserPreference(id));
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addUserPreference(@PathVariable Integer userId,
                                               @RequestBody @Valid UserPreferenceDTOIn userPreferenceDTOIn) {
        userPreferenceService.addUserPreference(userId, userPreferenceDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("User preference added successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPreferenceByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(userPreferenceService.getUserPreferenceByUserId(userId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUserPreference(@PathVariable Integer id, @RequestBody @Valid UserPreferenceDTOIn userPreferenceDTOIn) {
        userPreferenceService.updateUserPreference(id, userPreferenceDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("User preference updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUserPreference(@PathVariable Integer id) {
        userPreferenceService.deleteUserPreference(id);
        return ResponseEntity.status(200).body(new ApiResponse("User preference deleted successfully"));
    }

    // Resolves a workplace name with Nominatim and saves its coordinates.
    @PutMapping("/add-workplace/{userId}")
    public ResponseEntity<?> updateWorkplace(@PathVariable Integer userId, @RequestBody @Valid WorkplaceDTOIn workplaceDTOIn) {
        return ResponseEntity.status(200).body(userPreferenceService.updateWorkplace(userId, workplaceDTOIn));
    }
}
