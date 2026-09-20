package com.ltf.loanmanagement.kafka;

import com.ltf.loanmanagement.entity.LoanApplication;
import com.ltf.loanmanagement.entity.LoanStatus;
import com.ltf.loanmanagement.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Mock async credit-check service, consuming "loan-application-events".
 *
 * Consumer group "credit-check-service": if this service is scaled to N instances,
 * Kafka spreads the topic's 3 partitions across them (max useful parallelism = partition
 * count) while every event for a given loanApplicationId still lands on one instance,
 * because the producer keys by loanApplicationId.
 *
 * Idempotency: re-deliveries (e.g. after a consumer restart/rebalance before offset
 * commit) are safe because we only act when the loan is still PENDING - re-processing
 * an already-decided loan is a no-op instead of a duplicate decision.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreditCheckConsumer {

    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaTemplate<String, CreditCheckResultEvent> resultKafkaTemplate;

    @KafkaListener(
            topics = "loan-application-events",
            groupId = "credit-check-service",
            containerFactory = "loanApplicationEventListenerFactory"
    )
    @Transactional
    public void onLoanApplicationSubmitted(LoanApplicationEvent event) {
        log.info("Consumed LoanApplicationEvent loanId={} customerId={} amount={}",
                event.getLoanApplicationId(), event.getCustomerId(), event.getPrincipalAmount());

        Optional<LoanApplication> maybeLoan = loanApplicationRepository.findById(event.getLoanApplicationId());
        if (maybeLoan.isEmpty()) {
            log.warn("LoanApplication {} no longer exists - skipping credit check", event.getLoanApplicationId());
            return;
        }

        LoanApplication loan = maybeLoan.get();

        // Idempotency guard: only PENDING applications should be decided here.
        if (loan.getStatus() != LoanStatus.PENDING) {
            log.info("LoanApplication {} already in status {} - skipping duplicate credit check",
                    loan.getId(), loan.getStatus());
            return;
        }

        loan.setStatus(LoanStatus.UNDER_REVIEW);
        loanApplicationRepository.save(loan);

        CreditCheckDecision decision = runMockCreditCheck(event.getPrincipalAmount());

        loan.setStatus(decision.status());
        loanApplicationRepository.save(loan);

        CreditCheckResultEvent result = CreditCheckResultEvent.builder()
                .loanApplicationId(loan.getId())
                .decision(decision.status())
                .reason(decision.reason())
                .decidedAt(LocalDateTime.now())
                .build();

        resultKafkaTemplate.send("credit-check-results", String.valueOf(loan.getId()), result);

        log.info("Credit check complete for loanId={} -> {} ({})", loan.getId(), decision.status(), decision.reason());
    }

    /**
     * Deliberately simple heuristic standing in for a real bureau/credit-engine call:
     * requests above 20 lakh get flagged for manual review-equivalent rejection in this
     * mock, everything else is auto-approved. Swap this method for a real integration
     * without touching the Kafka plumbing.
     */
    private CreditCheckDecision runMockCreditCheck(BigDecimal principalAmount) {
        BigDecimal highRiskThreshold = new BigDecimal("2000000");
        if (principalAmount.compareTo(highRiskThreshold) > 0) {
            return new CreditCheckDecision(LoanStatus.REJECTED, "Requested principal exceeds auto-approval threshold");
        }
        return new CreditCheckDecision(LoanStatus.APPROVED, "Passed automated credit check");
    }

    private record CreditCheckDecision(LoanStatus status, String reason) {}
}
