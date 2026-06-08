package com.example.capstone3.Models;

import com.example.capstone3.Enums.MaintenancePriority;
import com.example.capstone3.Enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@NoArgsConstructor
@Getter @Setter
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lazy because user details are not always needed when loading a maintenance request.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Lazy because apartment details are not always needed when loading a maintenance request.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MaintenanceStatus status = MaintenanceStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private MaintenancePriority priority;

    @Column(name = "ai_category", length = 100)
    private String aiCategory;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @CurrentTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
