package com.ltf.loanmanagement.dto;

import com.ltf.loanmanagement.entity.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequest {

    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotNull(message = "loanType is required")
    private LoanType loanType;

    @NotNull(message = "principalAmount is required")
    @DecimalMin(value = "1000.00", message = "principalAmount must be at least 1000")
    private BigDecimal principalAmount;

    @NotNull(message = "interestRatePercent is required")
    @DecimalMin(value = "0.01", message = "interestRatePercent must be positive")
    @DecimalMax(value = "36.00", message = "interestRatePercent looks unreasonably high")
    private BigDecimal interestRatePercent;

    @NotNull(message = "tenureMonths is required")
    @Min(value = 3, message = "tenureMonths must be at least 3")
    @Max(value = 360, message = "tenureMonths must be at most 360")
    private Integer tenureMonths;
}
