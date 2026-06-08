package com.example.capstone3.Models;

import com.example.capstone3.Enums.RoommateStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoommateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @JsonIgnore
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    @JsonIgnore
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoommateStatus status; // PENDING, ACCEPTED, REJECTED, CANCELLED

    @Column(name = "createdAt")
    private LocalDate createdAt;
}