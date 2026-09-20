package com.ltf.loanmanagement.dto;

import com.ltf.loanmanagement.entity.LoanStatus;
import com.ltf.loanmanagement.entity.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRatePercent;
    private Integer tenureMonths;
    private LoanStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
