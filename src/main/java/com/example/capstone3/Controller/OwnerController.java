package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.OwnerDTOIn;
import com.example.capstone3.Service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.capstone3.Config.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
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
        requireSelf(principal, id);
        return ResponseEntity.status(200).body(ownerService.getOwner(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Integer id, @RequestBody @Valid OwnerDTOIn ownerDTOIn, @AuthenticationPrincipal AuthPrincipal principal) {
        requireSelf(principal, id);
        ownerService.updateOwner(id, ownerDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Owner updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Integer id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireSelf(principal, id);
        ownerService.deleteOwner(id);
        return ResponseEntity.status(200).body(new ApiResponse("Owner deleted successfully"));
    }

    private void requireSelf(AuthPrincipal principal, Integer id) {
        if (!principal.role().equals("OWNER") || !principal.id().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
