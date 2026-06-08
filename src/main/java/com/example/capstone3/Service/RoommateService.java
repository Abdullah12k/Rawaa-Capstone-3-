package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.Out.RoommateMatchDTOOut;
import com.example.capstone3.Models.User;
import com.example.capstone3.Models.UserPreference;
import com.example.capstone3.Repository.UserPreferenceRepository;
import com.example.capstone3.Repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoommateService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public List<RoommateMatchDTOOut> getAiRoommateMatches(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        UserPreference requesterPref = userPreferenceRepository.findUserPreferenceByUserId(userId);

        if (requesterPref == null || !Boolean.TRUE.equals(requesterPref.getLookingForRoommate())) {
            throw new ApiException("You must create a user preference and set lookingForRoommate to true to use this feature.");
        }
        if (requesterPref.getRoommateBudget() == null) {
            throw new ApiException("Roommate budget is required to find roommate matches.");
        }

        // 1. Database Pre-Filtering (+/- 1500 SAR budget variance)
        Double minBudget = requesterPref.getRoommateBudget() - 1500.0;
        Double maxBudget = requesterPref.getRoommateBudget() + 1500.0;

        List<UserPreference> candidates = userPreferenceRepository.findPotentialRoommates(
                user.getGender(), user.getId(), minBudget, maxBudget);

        if (candidates.isEmpty()) {
            throw new ApiException("No potential roommates found in your budget and gender criteria right now.");
        }

        // Limit to top 10 candidates to save AI tokens
        if (candidates.size() > 10) {
            candidates = candidates.subList(0, 10);
        }

        // 2. AI Behavioral Matchmaking
        try {
            String aiJsonString = aiService.getRoommateMatches(requesterPref, candidates);

            // Parse the JSON array into our DTO list
            List<RoommateMatchDTOOut> matches = objectMapper.readValue(aiJsonString, new TypeReference<List<RoommateMatchDTOOut>>() {});

            Map<Integer, UserPreference> allowedCandidates = candidates.stream()
                    .collect(Collectors.toMap(candidate -> candidate.getUser().getId(), Function.identity()));

            // Only return IDs that were included in the backend candidate list.
            List<RoommateMatchDTOOut> verifiedMatches = matches.stream()
                    .filter(match -> match.getCandidateId() != null)
                    .filter(match -> allowedCandidates.containsKey(match.getCandidateId()))
                    .peek(match -> match.setCandidateName(
                            allowedCandidates.get(match.getCandidateId()).getUser().getFullName()))
                    .toList();

            if (verifiedMatches.isEmpty()) {
                throw new ApiException("AI did not return any valid roommate candidates");
            }
            return verifiedMatches;

        } catch (Exception e) {
            if (e instanceof ApiException apiException) {
                throw apiException;
            }
            throw new ApiException("Failed to parse AI matches: " + e.getMessage());
        }
    }
}
