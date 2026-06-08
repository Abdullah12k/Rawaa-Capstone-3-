package com.example.capstone3.DTO.Out;

import com.example.capstone3.Enums.MaintenancePriority;
import com.example.capstone3.Enums.MaintenanceStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id", "userId", "apartmentId", "title", "status", "priority",
        "description", "aiCategory", "aiSummary", "createdAt", "completedAt"
})
@Data
public class MaintenanceRequestDTOOut {

    private Integer id;
    private Integer userId;
    private Integer apartmentId;
    private String title;
    private MaintenanceStatus status;
    private MaintenancePriority priority;
    private String description;
    private String aiCategory;
    private String aiSummary;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
