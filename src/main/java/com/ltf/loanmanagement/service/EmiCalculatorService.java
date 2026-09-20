package com.ltf.loanmanagement.service;

import com.ltf.loanmanagement.entity.LoanApplication;
import com.ltf.loanmanagement.entity.Repayment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard reducing-balance EMI calculation:
 *   EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
 * where P = principal, r = monthly interest rate, n = tenure in months.
 * Generates a full amortisation schedule (principal/interest split per installment).
 */
@Service
public class EmiCalculatorService {

    private static final MathContext MC = new MathContext(12);

    public BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, MC);

        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(onePlusRPowN, MC);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE, MC);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public List<Repayment> generateSchedule(LoanApplication loan) {
        BigDecimal emi = calculateMonthlyEmi(loan.getPrincipalAmount(), loan.getInterestRatePercent(), loan.getTenureMonths());
        BigDecimal monthlyRate = loan.getInterestRatePercent()
                .divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);

        BigDecimal outstandingPrincipal = loan.getPrincipalAmount();
        List<Repayment> schedule = new ArrayList<>();

        for (int month = 1; month <= loan.getTenureMonths(); month++) {
            BigDecimal interestComponent = outstandingPrincipal.multiply(monthlyRate, MC)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalComponent = emi.subtract(interestComponent).setScale(2, RoundingMode.HALF_UP);

            // Last installment absorbs any rounding drift so the schedule fully closes the loan.
            if (month == loan.getTenureMonths()) {
                principalComponent = outstandingPrincipal.setScale(2, RoundingMode.HALF_UP);
            }

            outstandingPrincipal = outstandingPrincipal.subtract(principalComponent);

            schedule.add(Repayment.builder()
                    .loanApplication(loan)
                    .installmentNumber(month)
                    .dueDate(LocalDate.now().plusMonths(month))
                    .emiAmount(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .paid(false)
                    .build());
        }
        return schedule;
    }
}
