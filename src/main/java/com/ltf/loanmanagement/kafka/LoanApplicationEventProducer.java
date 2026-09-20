package com.ltf.loanmanagement.kafka;

import com.ltf.loanmanagement.entity.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationEventProducer {

    private static final String TOPIC = "loan-application-events";

    private final KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate;

    /**
     * Keys the message by loanApplicationId (as a String) rather than sending null/round-robin.
     * Kafka routes same-key messages to the same partition, which guarantees this loan's
     * events are processed in order even though the topic has multiple partitions.
     */
    public void publishLoanApplicationSubmitted(LoanApplication loan) {
        LoanApplicationEvent event = LoanApplicationEvent.builder()
                .loanApplicationId(loan.getId())
                .customerId(loan.getCustomer().getId())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .eventTimestamp(LocalDateTime.now())
                .build();

        String key = String.valueOf(loan.getId());

        kafkaTemplate.send(TOPIC, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish LoanApplicationEvent for loanId={}", loan.getId(), ex);
            } else {
                log.info("Published LoanApplicationEvent loanId={} -> partition={} offset={}",
                        loan.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
