package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.ReservationDTOIn;
import com.example.capstone3.Service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

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
                                            @RequestBody @Valid ReservationDTOIn reservationDTOIn) {
        return ResponseEntity.status(200).body(reservationService.addReservation(userId, apartmentId, desiredMonthlyRent, reservationDTOIn));
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
    public ResponseEntity<?> getOwnerPendingReservations(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(reservationService.getOwnerPendingReservations(ownerId));
    }

    @GetMapping("/get/user/{userId}")
    public ResponseEntity<?> getReservationsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(reservationService.getReservationsByUserId(userId));
    }

    @PutMapping("/accept/{ownerId}/{reservationId}")
    public ResponseEntity<?> acceptReservation(@PathVariable Integer ownerId, @PathVariable Integer reservationId) {
        reservationService.acceptReservation(ownerId, reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation accepted successfully"));
    }

    @PutMapping("/reject/{ownerId}/{reservationId}")
    public ResponseEntity<?> rejectReservation(@PathVariable Integer ownerId, @PathVariable Integer reservationId) {
        reservationService.rejectReservation(ownerId, reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation rejected successfully"));
    }

    @PutMapping("/end/{userId}/{reservationId}")
    public ResponseEntity<?> endReservation(@PathVariable Integer userId, @PathVariable Integer reservationId) {
        reservationService.endReservation(userId, reservationId);
        return ResponseEntity.status(200).body(new ApiResponse("Reservation cancelled successfully"));
    }
}
