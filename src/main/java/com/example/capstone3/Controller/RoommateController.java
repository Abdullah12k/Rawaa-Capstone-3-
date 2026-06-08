package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.Out.RoommateMatchDTOOut;
import com.example.capstone3.Service.RoommateRequestService;
import com.example.capstone3.Service.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roommates")
@RequiredArgsConstructor
public class RoommateController {

    private final RoommateService roommateService;
    private final RoommateRequestService roommateRequestService;

    @GetMapping("/requests/{userId}")
    public ResponseEntity<?> getUserRoommateRequests(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(roommateRequestService.getUserRoommateRequests(userId));
    }

    @GetMapping("/matches/{userId}")
    public ResponseEntity<?> getAiRoommateMatches(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(roommateService.getAiRoommateMatches(userId));
    }

    @PostMapping("/request/{senderId}/{receiverId}")
    public ResponseEntity<?> sendRoommateRequest(@PathVariable Integer senderId, @PathVariable Integer receiverId) {
        roommateRequestService.sendRoommateRequest(senderId, receiverId);
        return ResponseEntity.status(200).body(new ApiResponse("Roommate request sent successfully."));
    }

    @PutMapping("/accept/{receiverId}/{requestId}")
    public ResponseEntity<?> acceptRoommateRequest(@PathVariable Integer receiverId, @PathVariable Integer requestId) {
        roommateRequestService.acceptRoommateRequest(receiverId, requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Request accepted! You are now linked as roommates and removed from the search pool."));
    }

    @PutMapping("/reject/{receiverId}/{requestId}")
    public ResponseEntity<?> rejectRoommateRequest(@PathVariable Integer receiverId, @PathVariable Integer requestId) {
        roommateRequestService.rejectRoommateRequest(receiverId, requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Roommate request rejected."));
    }

    @PutMapping("/dissolve/{userId}")
    public ResponseEntity<?> dissolveRoommate(@PathVariable Integer userId) {
        roommateRequestService.dissolveRoommate(userId);
        return ResponseEntity.status(200).body(new ApiResponse("Roommate link dissolved successfully."));
    }
}
