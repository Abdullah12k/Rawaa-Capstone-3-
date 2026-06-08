package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.RoommateRequestDTOOut;
import com.example.capstone3.Enums.RoommateStatus;
import com.example.capstone3.Models.RoommateRequest;
import com.example.capstone3.Models.User;
import com.example.capstone3.Models.UserPreference;
import com.example.capstone3.Repository.RoommateRequestRepository;
import com.example.capstone3.Repository.UserPreferenceRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoommateRequestService {

    private final RoommateRequestRepository roommateRequestRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final WhatsAppService whatsAppService;

    public List<RoommateRequestDTOOut> getUserRoommateRequests(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found!");
        }

        List<RoommateRequestDTOOut> result = new ArrayList<>();
        for (RoommateRequest request :
                roommateRequestRepository.findBySenderAndStatusOrReceiverAndStatus(
                        user, RoommateStatus.PENDING,
                        user, RoommateStatus.PENDING)) {
            RoommateRequestDTOOut dto = new RoommateRequestDTOOut();
            dto.setId(request.getId());
            dto.setSenderId(request.getSender().getId());
            dto.setSenderName(request.getSender().getFullName());
            dto.setReceiverId(request.getReceiver().getId());
            dto.setReceiverName(request.getReceiver().getFullName());
            dto.setStatus(request.getStatus());
            dto.setCreatedAt(request.getCreatedAt());
            dto.setDirection(request.getSender().getId().equals(userId)
                    ? "SENT" : "RECEIVED");
            result.add(dto);
        }
        return result;
    }

    public void sendRoommateRequest(Integer senderId, Integer receiverId) {
        if (senderId.equals(receiverId)) {
            throw new ApiException("You cannot send a roommate request to yourself!");
        }

        User sender = userRepository.findUserById(senderId);
        User receiver = userRepository.findUserById(receiverId);

        if (sender == null || receiver == null) {
            throw new ApiException("Sender or Receiver not found!");
        }

        // Prevent sending a request if either user already has a roommate
        if (sender.getCurrentRoommateId() != null || receiver.getCurrentRoommateId() != null) {
            throw new ApiException("One of the users is no longer available for roommating.");
        }

        UserPreference senderPreference = userPreferenceRepository.findUserPreferenceByUserId(senderId);
        UserPreference receiverPreference = userPreferenceRepository.findUserPreferenceByUserId(receiverId);
        if (senderPreference == null || receiverPreference == null
                || !Boolean.TRUE.equals(senderPreference.getLookingForRoommate())
                || !Boolean.TRUE.equals(receiverPreference.getLookingForRoommate())) {
            throw new ApiException("Both users must be available for roommate matching.");
        }

        boolean duplicateRequest =
                roommateRequestRepository.existsBySenderAndReceiverAndStatus(
                        sender, receiver, RoommateStatus.PENDING)
                        || roommateRequestRepository.existsBySenderAndReceiverAndStatus(
                        receiver, sender, RoommateStatus.PENDING);
        if (duplicateRequest) {
            throw new ApiException("A pending roommate request already exists between these users.");
        }

        RoommateRequest request = new RoommateRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(RoommateStatus.PENDING);
        request.setCreatedAt(LocalDate.now());

        roommateRequestRepository.save(request);
        whatsAppService.notifyRoommateRequestReceived(receiver, sender);
    }

    @Transactional
    public void acceptRoommateRequest(Integer receiverId, Integer requestId) {
        RoommateRequest request = roommateRequestRepository.findRoommateRequestById(requestId);

        if (request == null) {
            throw new ApiException("Roommate request not found!");
        }

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new ApiException("You are not authorized to accept this request!");
        }

        if (!request.getStatus().equals(RoommateStatus.PENDING)) {
            throw new ApiException("Only PENDING requests can be accepted!");
        }

        User sender = request.getSender();
        User receiver = request.getReceiver();

        if (sender.getCurrentRoommateId() != null || receiver.getCurrentRoommateId() != null) {
            throw new ApiException("One of the users is no longer available for roommating.");
        }

        UserPreference senderPref = userPreferenceRepository.findUserPreferenceByUserId(sender.getId());
        UserPreference receiverPref = userPreferenceRepository.findUserPreferenceByUserId(receiver.getId());
        if (senderPref == null || receiverPref == null
                || !Boolean.TRUE.equals(senderPref.getLookingForRoommate())
                || !Boolean.TRUE.equals(receiverPref.getLookingForRoommate())) {
            throw new ApiException("One of the users is no longer available for roommate matching.");
        }

        // 1. Change this specific request status to ACCEPTED
        request.setStatus(RoommateStatus.ACCEPTED);
        roommateRequestRepository.save(request);

        // 2. Link the users together
        sender.setCurrentRoommateId(receiver.getId());
        receiver.setCurrentRoommateId(sender.getId());
        userRepository.save(sender);
        userRepository.save(receiver);

        // 3. Take them both off the market
        senderPref.setLookingForRoommate(false);
        receiverPref.setLookingForRoommate(false);
        userPreferenceRepository.save(senderPref);
        userPreferenceRepository.save(receiverPref);

        // 4. The Domino Effect (Cancel all other pending requests involving these two users)
        cancelOtherPendingRequests(sender);
        cancelOtherPendingRequests(receiver);

        whatsAppService.notifyRoommateRequestAccepted(sender, receiver);
    }

    // Helper Method for the Domino Effect
    private void cancelOtherPendingRequests(User user) {
        List<RoommateRequest> pendingRequests = roommateRequestRepository.findBySenderAndStatusOrReceiverAndStatus(
                user, RoommateStatus.PENDING, user, RoommateStatus.PENDING);

        for (RoommateRequest req : pendingRequests) {
            req.setStatus(RoommateStatus.CANCELLED);
            roommateRequestRepository.save(req);
        }
    }

    public void rejectRoommateRequest(Integer receiverId, Integer requestId) {
        RoommateRequest request = roommateRequestRepository.findRoommateRequestById(requestId);

        if (request == null) {
            throw new ApiException("Roommate request not found!");
        }

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new ApiException("You are not authorized to reject this request!");
        }

        if (!request.getStatus().equals(RoommateStatus.PENDING)) {
            throw new ApiException("Only PENDING requests can be rejected!");
        }

        request.setStatus(RoommateStatus.REJECTED);
        roommateRequestRepository.save(request);
        whatsAppService.notifyRoommateRequestRejected(
                request.getSender(), request.getReceiver());
    }

    @Transactional
    public void dissolveRoommate(Integer userId) {
        User user1 = userRepository.findUserById(userId);

        if (user1 == null || user1.getCurrentRoommateId() == null) {
            throw new ApiException("You do not currently have a roommate to dissolve.");
        }

        User user2 = userRepository.findUserById(user1.getCurrentRoommateId());

        // Always clean the requesting user's stale or valid roommate link.
        user1.setCurrentRoommateId(null);
        userRepository.save(user1);

        // Only clear the other user when the relationship is reciprocal.
        if (user2 != null && user1.getId().equals(user2.getCurrentRoommateId())) {
            user2.setCurrentRoommateId(null);
            userRepository.save(user2);
        }

        UserPreference user1Preference = userPreferenceRepository.findUserPreferenceByUserId(user1.getId());
        if (user1Preference != null) {
            user1Preference.setLookingForRoommate(true);
            userPreferenceRepository.save(user1Preference);
        }
        if (user2 != null && user2.getCurrentRoommateId() == null) {
            UserPreference user2Preference = userPreferenceRepository.findUserPreferenceByUserId(user2.getId());
            if (user2Preference != null) {
                user2Preference.setLookingForRoommate(true);
                userPreferenceRepository.save(user2Preference);
            }
        }
    }
}
