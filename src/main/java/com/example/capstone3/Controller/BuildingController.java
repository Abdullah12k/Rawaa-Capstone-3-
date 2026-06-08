package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.BuildingDTOIn;
import com.example.capstone3.Service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/building")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping("/get")
    public ResponseEntity<?> getBuildings() {
        return ResponseEntity.status(200).body(buildingService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getBuilding(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(buildingService.getBuilding(id));
    }

    @PostMapping("/add/{owner_id}")
    public ResponseEntity<?> addBuilding(@RequestBody @Valid BuildingDTOIn buildingDTOIn, @PathVariable Integer owner_id) {
        buildingService.addBuilding(buildingDTOIn, owner_id);
        return ResponseEntity.status(200).body(new ApiResponse("Building added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBuilding(@PathVariable Integer id, @RequestBody @Valid BuildingDTOIn buildingDTOIn) {
        buildingService.updateBuilding(id, buildingDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Building updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBuilding(@PathVariable Integer id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.status(200).body(new ApiResponse("Building deleted successfully"));
    }
}
