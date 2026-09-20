package com.ltf.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "repayments", indexes = {
        @Index(name = "idx_repayment_loan", columnList = "loan_application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal principalComponent;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal interestComponent;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    private LocalDate paidDate;
}
