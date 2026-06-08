package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.OwnerDTOIn;
import com.example.capstone3.Service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/get")
    public ResponseEntity<?> getOwners() {
        return ResponseEntity.status(200).body(ownerService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getOwner(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(ownerService.getOwner(id));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addOwner(@RequestBody @Valid OwnerDTOIn ownerDTOIn) {
        ownerService.addOwner(ownerDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Owner added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Integer id, @RequestBody @Valid OwnerDTOIn ownerDTOIn) {
        ownerService.updateOwner(id, ownerDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Owner updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Integer id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.status(200).body(new ApiResponse("Owner deleted successfully"));
    }
}
