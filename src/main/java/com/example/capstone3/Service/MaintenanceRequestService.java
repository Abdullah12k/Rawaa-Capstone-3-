package com.example.capstone3.Service;

import com.example.capstone3.Api.ApiException;
import com.example.capstone3.DTO.In.MaintenanceRequestDTOIn;
import com.example.capstone3.DTO.Out.BuildingMaintenanceSummaryDTOOut;
import com.example.capstone3.DTO.Out.MaintenanceRequestDTOOut;
import com.example.capstone3.Enums.ApartmentStatus;
import com.example.capstone3.Enums.ContractStatus;
import com.example.capstone3.Enums.MaintenancePriority;
import com.example.capstone3.Enums.MaintenanceStatus;
import com.example.capstone3.Models.Apartment;
import com.example.capstone3.Models.Building;
import com.example.capstone3.Models.Contract;
import com.example.capstone3.Models.MaintenanceRequest;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.ApartmentRepository;
import com.example.capstone3.Repository.BuildingRepository;
import com.example.capstone3.Repository.ContractRepository;
import com.example.capstone3.Repository.MaintenanceRepository;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRequestService {

    // Uses OpenAI to classify maintenance requests and summarize building issues.
    private final MaintenanceRepository maintenanceRepository;
    private final UserRepository        userRepository;
    private final ApartmentRepository   apartmentRepository;
    private final ContractRepository    contractRepository;
    private final BuildingRepository    buildingRepository;
    private final OwnerRepository       ownerRepository;
    private final AiService             aiService;
    private final WhatsAppService       whatsAppService;


    public List<MaintenanceRequestDTOOut> getAll() {
        List<MaintenanceRequestDTOOut> result = new ArrayList<>();
        for (MaintenanceRequest req : maintenanceRepository.findAll()) {
            result.add(convertToDTO(req));
        }
        return result;
    }


    public MaintenanceRequestDTOOut getMaintenanceRequest(Integer id) {
        MaintenanceRequest req = maintenanceRepository.findMaintenanceRequestById(id);
        if (req == null) {
            throw new ApiException("Maintenance request not found");
        }
        return convertToDTO(req);
    }


    public List<MaintenanceRequestDTOOut> getUserMaintenanceRequests(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        List<MaintenanceRequestDTOOut> result = new ArrayList<>();
        for (MaintenanceRequest req : maintenanceRepository.findMaintenanceRequestsByUserId(userId)) {
            result.add(convertToDTO(req));
        }
        return result;
    }


    public List<MaintenanceRequestDTOOut> getApartmentMaintenanceRequests(Integer apartmentId) {
        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }
        List<MaintenanceRequestDTOOut> result = new ArrayList<>();
        for (MaintenanceRequest req : maintenanceRepository.findMaintenanceRequestsByApartmentId(apartmentId)) {
            result.add(convertToDTO(req));
        }
        return result;
    }


    public void createMaintenanceRequest(Integer userId, Integer apartmentId, MaintenanceRequestDTOIn dto,
                                         String language) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        Apartment apartment = apartmentRepository.findApartmentById(apartmentId);
        if (apartment == null) {
            throw new ApiException("Apartment not found");
        }

        if (apartment.getStatus() != ApartmentStatus.RENTED) {
            throw new ApiException("Apartment is not currently rented");
        }

        Contract contract = contractRepository.findByUserAndApartmentAndStatus(
                userId, apartmentId, ContractStatus.ACTIVE);
        if (contract == null) {
            throw new ApiException("No active contract found for this user and apartment");
        }

        // OpenAI classifies the issue after backend contract checks pass.
        String aiResponse = aiService.generateText(
                buildMaintenanceAnalysisPrompt(dto.getTitle(), dto.getDescription()), language);

        MaintenanceRequest req = new MaintenanceRequest();
        req.setUser(user);
        req.setApartment(apartment);
        req.setTitle(dto.getTitle());
        req.setDescription(dto.getDescription());
        req.setStatus(MaintenanceStatus.PENDING);
        req.setAiCategory(extractAiField(aiResponse, "Category"));
        req.setAiSummary(aiService.cleanAiText(extractAiField(aiResponse, "Summary")));
        req.setPriority(parseAiPriority(aiResponse));

        maintenanceRepository.save(req);

        whatsAppService.notifyOwnerNewMaintenanceRequest(apartment.getOwner(), req);
    }


    public void startMaintenanceRequest(Integer ownerId, Integer requestId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        MaintenanceRequest req = maintenanceRepository.findMaintenanceRequestById(requestId);
        if (req == null) {
            throw new ApiException("Maintenance request not found");
        }
        if (!req.getApartment().getBuilding().getOwner().getId().equals(ownerId)) {
            throw new ApiException("You are not authorized to update this maintenance request");
        }
        if (req.getStatus() != MaintenanceStatus.PENDING) {
            throw new ApiException("Only PENDING requests can be started");
        }
        req.setStatus(MaintenanceStatus.IN_PROGRESS);
        maintenanceRepository.save(req);

        whatsAppService.notifyTenantMaintenanceUpdated(req.getUser(), req);
    }


    public void completeMaintenanceRequest(Integer ownerId, Integer requestId) {
        Owner owner = ownerRepository.findOwnerById(ownerId);
        if (owner == null) {
            throw new ApiException("Owner not found");
        }
        MaintenanceRequest req = maintenanceRepository.findMaintenanceRequestById(requestId);
        if (req == null) {
            throw new ApiException("Maintenance request not found");
        }
        if (!req.getApartment().getBuilding().getOwner().getId().equals(ownerId)) {
            throw new ApiException("You are not authorized to update this maintenance request");
        }
        if (req.getStatus() != MaintenanceStatus.IN_PROGRESS) {
            throw new ApiException("Only IN_PROGRESS requests can be completed");
        }
        req.setStatus(MaintenanceStatus.COMPLETED);
        req.setCompletedAt(LocalDateTime.now());
        maintenanceRepository.save(req);

        whatsAppService.notifyTenantMaintenanceUpdated(req.getUser(), req);
    }


    public void updateMaintenanceRequest(Integer userId, Integer id,
                                         MaintenanceRequestDTOIn dto, String language) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        MaintenanceRequest req = maintenanceRepository.findMaintenanceRequestById(id);
        if (req == null) {
            throw new ApiException("Maintenance request not found");
        }
        if (!req.getUser().getId().equals(userId)) {
            throw new ApiException("Only the tenant who created this request can update it");
        }
        if (req.getStatus() != MaintenanceStatus.PENDING) {
            throw new ApiException("Only PENDING requests can be updated");
        }
        req.setTitle(dto.getTitle());
        req.setDescription(dto.getDescription());

        // Reclassify the request because its issue details changed.
        String aiResponse = aiService.generateText(
                buildMaintenanceAnalysisPrompt(dto.getTitle(), dto.getDescription()), language);
        req.setAiCategory(extractAiField(aiResponse, "Category"));
        req.setAiSummary(aiService.cleanAiText(extractAiField(aiResponse, "Summary")));
        req.setPriority(parseAiPriority(aiResponse));

        maintenanceRepository.save(req);
    }


    public void deleteMaintenanceRequest(Integer userId, Integer id) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        MaintenanceRequest req = maintenanceRepository.findMaintenanceRequestById(id);
        if (req == null) {
            throw new ApiException("Maintenance request not found");
        }
        if (!req.getUser().getId().equals(userId)) {
            throw new ApiException("Only the tenant who created this request can delete it");
        }
        if (req.getStatus() != MaintenanceStatus.PENDING) {
            throw new ApiException("Only PENDING requests can be deleted");
        }
        maintenanceRepository.deleteById(id);
    }


    // Summarizes maintenance patterns across one building using stored requests.
    public BuildingMaintenanceSummaryDTOOut getBuildingMaintenanceSummary(Integer buildingId,
                                                                           String language) {
        Building building = buildingRepository.findBuildingById(buildingId);
        if (building == null) {
            throw new ApiException("Building not found");
        }

        List<MaintenanceRequest> requests = maintenanceRepository.findMaintenanceRequestsByApartment_Building_Id(buildingId);
        if (requests.isEmpty()) {
            throw new ApiException("No maintenance requests found for this building");
        }

        String aiResponse = aiService.generateText(buildBuildingSummaryPrompt(building, requests), language);

        BuildingMaintenanceSummaryDTOOut response = new BuildingMaintenanceSummaryDTOOut();
        response.setSummary(aiService.cleanAiText(aiResponse));

        return response;
    }


    // Instruct OpenAI to return category, priority, and summary in a fixed format.
    private String buildMaintenanceAnalysisPrompt(String title, String description) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a property maintenance analyst. Analyze this maintenance request and respond in exactly this format:\n\n");
        prompt.append("Category: [one of: Plumbing, Electrical, HVAC, Elevator, Internet, Appliance, Water Leakage, Other]\n");
        prompt.append("Priority: [one of: LOW, MEDIUM, HIGH, URGENT]\n");
        prompt.append("Summary: [2-3 sentence professional summary of the issue and recommended urgency]\n\n");
        prompt.append("Keep the labels Category, Priority, and Summary in English so the system can read them.\n");
        prompt.append("=== MAINTENANCE REQUEST ===\n");
        prompt.append("Title: ").append(title).append("\n");
        prompt.append("Description: ").append(description).append("\n\n");
        prompt.append("Use URGENT for immediate safety risks or severe damage.\n");
        prompt.append("Use HIGH for issues significantly affecting daily living.\n");
        prompt.append("Use MEDIUM for disruptive but non-critical issues.\n");
        prompt.append("Use LOW for minor inconveniences.");

        return prompt.toString();
    }

    // Give OpenAI current maintenance facts for a short owner-friendly overview.
    private String buildBuildingSummaryPrompt(Building building, List<MaintenanceRequest> requests) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Summarize the current maintenance activity for a residential building in Saudi Arabia.\n\n");

        prompt.append("=== BUILDING ===\n");
        prompt.append("Name: ").append(building.getName()).append("\n");
        prompt.append("District: ").append(building.getDistrict()).append("\n");
        prompt.append("City: ").append(building.getCity()).append("\n");
        prompt.append("Total Maintenance Requests: ").append(requests.size()).append("\n\n");

        prompt.append("=== MAINTENANCE REQUESTS ===\n");
        for (int i = 0; i < requests.size(); i++) {
            MaintenanceRequest req = requests.get(i);
            prompt.append("\nRequest ").append(i + 1).append(":\n");
            prompt.append("Title: ").append(req.getTitle()).append("\n");
            prompt.append("Status: ").append(req.getStatus()).append("\n");
            prompt.append("Priority: ").append(req.getPriority() != null ? req.getPriority() : "Not set").append("\n");
            prompt.append("Category: ").append(req.getAiCategory() != null ? req.getAiCategory() : "Uncategorized").append("\n");
        }

        prompt.append("\n=== OUTPUT INSTRUCTIONS ===\n");
        prompt.append("Write one natural overview of 2 to 4 concise sentences.\n");
        prompt.append("Quickly describe the issues that currently exist.\n");
        prompt.append("Mention urgent requests when present.\n");
        prompt.append("Mention recurring or common issues when present.\n");
        prompt.append("If there is one request, summarize its issue, urgency, and current status naturally.\n");
        prompt.append("Do not include recommendations, suggested actions, preventive actions, root cause analysis, strategies, or advice.\n");
        prompt.append("Do not use headings, numbered sections, bullet lists, or Markdown.\n");
        prompt.append("Do not repeat information or add unnecessary details.");

        return prompt.toString();
    }


    // Read a named value from OpenAI's structured maintenance response.
    private String extractAiField(String aiResponse, String field) {
        if (aiResponse == null || aiResponse.isBlank()) return null;
        for (String line : aiResponse.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith(field + ":")) {
                return trimmed.substring(field.length() + 1).trim();
            }
        }
        return null;
    }

    // Convert OpenAI's priority text to the project enum with a safe default.
    private MaintenancePriority parseAiPriority(String aiResponse) {
        String priorityValue = extractAiField(aiResponse, "Priority");
        if (priorityValue != null) {
            try {
                return MaintenancePriority.valueOf(priorityValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MaintenancePriority.MEDIUM;
            }
        }
        return MaintenancePriority.MEDIUM;
    }


    public MaintenanceRequestDTOOut convertToDTO(MaintenanceRequest req) {
        MaintenanceRequestDTOOut dto = new MaintenanceRequestDTOOut();
        dto.setId(req.getId());
        dto.setUserId(req.getUser().getId());
        dto.setApartmentId(req.getApartment().getId());
        dto.setTitle(req.getTitle());
        dto.setDescription(req.getDescription());
        dto.setStatus(req.getStatus());
        dto.setPriority(req.getPriority());
        dto.setAiCategory(req.getAiCategory());
        dto.setAiSummary(req.getAiSummary());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setCompletedAt(req.getCompletedAt());
        return dto;
    }
}
