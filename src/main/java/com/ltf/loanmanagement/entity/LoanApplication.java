package com.ltf.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_applications", indexes = {
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many loan applications belong to one customer.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanType loanType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRatePercent; // annual %, e.g. 10.50

    @Column(nullable = false)
    private Integer tenureMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // One loan application generates a full EMI repayment schedule.
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Repayment> repayments = new ArrayList<>();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
