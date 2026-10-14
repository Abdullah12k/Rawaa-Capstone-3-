package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.OwnerDTOIn;
import com.example.capstone3.Service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.capstone3.Config.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> getOwner(@PathVariable Integer id, @AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.status(200).body(ownerService.getOwner(principal.requireOwner(id)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Integer id, @RequestBody @Valid OwnerDTOIn ownerDTOIn, @AuthenticationPrincipal AuthPrincipal principal) {
        ownerService.updateOwner(principal.requireOwner(id), ownerDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Owner updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Integer id, @AuthenticationPrincipal AuthPrincipal principal) {
        ownerService.deleteOwner(principal.requireOwner(id));
        return ResponseEntity.status(200).body(new ApiResponse("Owner deleted successfully"));
    }

}
