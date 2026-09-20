package com.ltf.loanmanagement.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ltf.loanmanagement.entity.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event payload published to the "loan-application-events" topic whenever a new
 * loan application is submitted. Kept intentionally small/flat (event-carried
 * state transfer) so consumers don't need a synchronous call back to this service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationEvent {
    private Long loanApplicationId;
    private Long customerId;
    private LoanType loanType;
    private BigDecimal principalAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;
}
