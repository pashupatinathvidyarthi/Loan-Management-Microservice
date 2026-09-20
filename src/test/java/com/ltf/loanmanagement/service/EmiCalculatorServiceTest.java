package com.ltf.loanmanagement.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EmiCalculatorServiceTest {

    private final EmiCalculatorService emiCalculatorService = new EmiCalculatorService();

    @Test
    void calculatesMonthlyEmiForStandardPersonalLoan() {
        // 1,00,000 principal, 12% annual, 12 months -> known EMI ~ 8884.88
        BigDecimal emi = emiCalculatorService.calculateMonthlyEmi(
                new BigDecimal("100000"), new BigDecimal("12"), 12);

        assertThat(emi).isEqualByComparingTo(new BigDecimal("8884.88"));
    }

    @Test
    void zeroInterestSplitsPrincipalEvenly() {
        BigDecimal emi = emiCalculatorService.calculateMonthlyEmi(
                new BigDecimal("12000"), BigDecimal.ZERO, 12);

        assertThat(emi).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}
