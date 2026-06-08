package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.ApartmentDTOIn;
import com.example.capstone3.DTO.Out.ApartmentDTOOut;
import com.example.capstone3.Service.ApartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/apartment")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    @GetMapping("/get")
    public ResponseEntity<?> getApartments() {
        return ResponseEntity.status(200).body(apartmentService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getApartment(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(apartmentService.getApartment(id));
    }

    @PostMapping("/add/{ownerId}/{buildingId}")
    public ResponseEntity<?> addApartment(@PathVariable Integer ownerId, @PathVariable Integer buildingId, @RequestBody @Valid ApartmentDTOIn apartmentDTOIn) {
        apartmentService.addApartment(ownerId, buildingId, apartmentDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Apartment added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateApartment(@PathVariable Integer id, @RequestBody @Valid ApartmentDTOIn apartmentDTOIn) {
        apartmentService.updateApartment(id, apartmentDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Apartment updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteApartment(@PathVariable Integer id) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.status(200).body(new ApiResponse("Apartment deleted successfully"));
    }

    @GetMapping("/get/underpriced")
    public ResponseEntity<?> getUnderpricedApartments() {
        return ResponseEntity.status(200).body(apartmentService.getUnderpricedApartments());
    }

    @GetMapping("/low-rated/{ownerId}/{buildingId}")
    public ResponseEntity<?> getLowRatedApartmentsByBuilding(@PathVariable Integer ownerId, @PathVariable Integer buildingId) {
        return ResponseEntity.status(200).body(apartmentService.getLowRatedApartmentsByBuilding(ownerId, buildingId));
    }

    @GetMapping("/dashboard/{ownerId}")
    public ResponseEntity<?> getOwnerDashboard(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(apartmentService.getOwnerDashboard(ownerId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchApartments(@RequestParam(required = false) Double minRent, @RequestParam(required = false) Double maxRent,
                                              @RequestParam(required = false) Integer bedrooms, @RequestParam(required = false) String district,
                                              @RequestParam(required = false) Boolean isFurnished) {
        return ResponseEntity.status(200).body(apartmentService.searchApartments(minRent, maxRent, bedrooms, district, isFurnished));
    }

    @PutMapping("/available/{ownerId}/{apartmentId}")
    public ResponseEntity<?> makeApartmentAvailable(@PathVariable Integer ownerId, @PathVariable Integer apartmentId) {
        apartmentService.toggleAvailableMode(ownerId, apartmentId);
        return ResponseEntity.status(200).body(new ApiResponse("Apartment is now available"));
    }

    @PutMapping("/toggle-maintenance/{ownerId}/{apartmentId}")
    public ResponseEntity<?> makeApartmentUnderMaintenance(@PathVariable Integer ownerId, @PathVariable Integer apartmentId) {
        apartmentService.toggleMaintenanceMode(ownerId, apartmentId);
        return ResponseEntity.status(200).body(new ApiResponse("Apartment is now under maintenance"));
    }

    @GetMapping("/next-available/{apartmentId}")
    public ResponseEntity<?> getNextAvailability(@PathVariable Integer apartmentId) {
        return ResponseEntity.status(200).body(apartmentService.getNextAvailability(apartmentId));
    }

    @GetMapping("/available-on/{apartmentId}/{date}")
    public ResponseEntity<?> checkAvailabilityOnDate(@PathVariable Integer apartmentId, @PathVariable LocalDate date) {
        return ResponseEntity.status(200).body(apartmentService.checkAvailabilityOnDate(apartmentId, date));
    }

    @GetMapping("/flagged/{ownerId}")
    public ResponseEntity<?> getFlaggedApartmentsByCancellationRate(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(apartmentService.getFlaggedApartmentsByCancellationRate(ownerId));
    }


}
