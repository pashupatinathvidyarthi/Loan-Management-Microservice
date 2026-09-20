package com.ltf.loanmanagement.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ltf.loanmanagement.entity.LoanStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Result published by the credit-check consumer to "credit-check-results" once it
 * finishes processing a LoanApplicationEvent. A downstream notification service
 * (out of scope here) could subscribe to this to email/SMS the applicant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCheckResultEvent {
    private Long loanApplicationId;
    private LoanStatus decision;
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime decidedAt;
}
