package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.Config.AuthPrincipal;
import com.example.capstone3.DTO.In.ReservationDTOIn;
import com.example.capstone3.Service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // Like Mena and Feena, protected methods receive the signed-in account through
    // @AuthenticationPrincipal. The URL account ID is retained for existing clients.

    @GetMapping("/get")
    public ResponseEntity<?> getReservations() {
        return ResponseEntity.status(200).body(reservationService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(reservationService.getReservation(id));
    }

    @PostMapping("/add/{userId}/{apartmentId}")
    public ResponseEntity<?> addReservation(@PathVariable Integer userId,
                                            @PathVariable Integer apartmentId,
                                            @RequestParam(required = false) Double desiredMonthlyRent,
                                            @RequestBody @Valid ReservationDTOIn reservationDTOIn,
                                            @AuthenticationPrincipal AuthPrincipal renter) {
        return ResponseEntity.status(200).body(reservationService.addReservation(renter.requireUser(userId), apartmentId, desiredMonthlyRent, reservationDTOIn));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Integer id, @RequestBody @Valid ReservationDTOIn reservationDTOIn) {
        reservationService.updateReservation(id, reservationDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation deleted successfully"));
    }


    @GetMapping("/get/pending")
    public ResponseEntity<?> getPendingReservations() {
        return ResponseEntity.status(200).body(reservationService.getPendingReservations());
    }

    @GetMapping("/get/owner/{ownerId}/pending")
    public ResponseEntity<?> getOwnerPendingReservations(@PathVariable Integer ownerId, @AuthenticationPrincipal AuthPrincipal owner) {
        return ResponseEntity.status(200).body(reservationService.getOwnerPendingReservations(owner.requireOwner(ownerId)));
    }

    @GetMapping("/get/owner/{ownerId}")
    public ResponseEntity<?> getOwnerReservations(@PathVariable Integer ownerId, @AuthenticationPrincipal AuthPrincipal owner) {
        return ResponseEntity.ok(reservationService.getOwnerReservations(owner.requireOwner(ownerId)));
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getReservationsByUserId(@PathVariable Integer userId, @AuthenticationPrincipal AuthPrincipal renter) {
        return ResponseEntity.status(200).body(reservationService.getReservationsByUserId(renter.requireUser(userId)));
    }

    @PutMapping("/accept/{ownerId}/{reservationId}")
    public ResponseEntity<?> acceptReservation(@PathVariable Integer ownerId, @PathVariable Integer reservationId, @AuthenticationPrincipal AuthPrincipal owner) {
        reservationService.acceptReservation(owner.requireOwner(ownerId), reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation accepted successfully"));
    }

    @PutMapping("/reject/{ownerId}/{reservationId}")
    public ResponseEntity<?> rejectReservation(@PathVariable Integer ownerId, @PathVariable Integer reservationId, @AuthenticationPrincipal AuthPrincipal owner) {
        reservationService.rejectReservation(owner.requireOwner(ownerId), reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation rejected successfully"));
    }

    @PutMapping("/end/{userId}/{reservationId}")
    public ResponseEntity<?> endReservation(@PathVariable Integer userId, @PathVariable Integer reservationId, @AuthenticationPrincipal AuthPrincipal renter) {
        reservationService.endReservation(renter.requireUser(userId), reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation cancelled successfully"));
    }
}
