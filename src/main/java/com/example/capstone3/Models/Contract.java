package com.example.capstone3.Models;

import com.example.capstone3.Enums.ContractStatus;
import com.example.capstone3.Enums.RenewalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lazy because reservation details are loaded only for contract workflows.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "contract_number", nullable = false, unique = true, length = 100)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false)
    private Double monthlyRent;

    @Column(name = "security_deposit")
    private Double securityDeposit;

    @Column(name = "signed", nullable = false)
    private Boolean signed;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false, length = 20)
    private ContractStatus contractStatus;

    @CurrentTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "termination_reason", length = 1000)
    private String terminationReason;

    @Column(name = "renewal_requested_months")
    private Integer renewalRequestedMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "renewal_status", length = 20)
    private RenewalStatus renewalStatus;
}
