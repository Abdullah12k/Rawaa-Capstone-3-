package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.MaintenanceRequestDTOIn;
import com.example.capstone3.Service.MaintenanceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance-request")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    @GetMapping("/get")
    public ResponseEntity<?> getMaintenanceRequests() {
        return ResponseEntity.status(200).body(maintenanceRequestService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMaintenanceRequest(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(maintenanceRequestService.getMaintenanceRequest(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMaintenanceRequests(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(maintenanceRequestService.getUserMaintenanceRequests(userId));
    }

    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<?> getApartmentMaintenanceRequests(@PathVariable Integer apartmentId) {
        return ResponseEntity.status(200).body(maintenanceRequestService.getApartmentMaintenanceRequests(apartmentId));
    }

    // Generates an AI summary of maintenance patterns for a building.
    @GetMapping("/building-summary/{buildingId}")
    public ResponseEntity<?> getBuildingMaintenanceSummary(@PathVariable Integer buildingId,
                                                           @RequestParam(defaultValue = "EN") String language) {
        return ResponseEntity.status(200).body(maintenanceRequestService.getBuildingMaintenanceSummary(buildingId, language));
    }

    // Creates a request and uses AI to classify its category and priority.
    @PostMapping("/add/{userId}/{apartmentId}")
    public ResponseEntity<?> addMaintenanceRequest(@PathVariable Integer userId, @PathVariable Integer apartmentId,
                                                   @RequestBody @Valid MaintenanceRequestDTOIn dto,
                                                   @RequestParam(defaultValue = "EN") String language) {
        maintenanceRequestService.createMaintenanceRequest(userId, apartmentId, dto, language);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance request created successfully"));
    }

    @PutMapping("/start/{ownerId}/{requestId}")
    public ResponseEntity<?> startMaintenanceRequest(@PathVariable Integer ownerId, @PathVariable Integer requestId) {
        maintenanceRequestService.startMaintenanceRequest(ownerId, requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance request started"));
    }

    @PutMapping("/complete/{ownerId}/{requestId}")
    public ResponseEntity<?> completeMaintenanceRequest(@PathVariable Integer ownerId, @PathVariable Integer requestId) {
        maintenanceRequestService.completeMaintenanceRequest(ownerId, requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance request completed"));
    }

    @PutMapping("/update/{userId}/{id}")
    public ResponseEntity<?> updateMaintenanceRequest(@PathVariable Integer userId,
                                                      @PathVariable Integer id,
                                                      @RequestBody @Valid MaintenanceRequestDTOIn dto,
                                                      @RequestParam(defaultValue = "EN") String language) {
        maintenanceRequestService.updateMaintenanceRequest(userId, id, dto, language);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance request updated successfully"));
    }

    @DeleteMapping("/delete/{userId}/{id}")
    public ResponseEntity<?> deleteMaintenanceRequest(@PathVariable Integer userId,
                                                      @PathVariable Integer id) {
        maintenanceRequestService.deleteMaintenanceRequest(userId, id);
        return ResponseEntity.status(200).body(new ApiResponse("Maintenance request deleted successfully"));
    }
}
