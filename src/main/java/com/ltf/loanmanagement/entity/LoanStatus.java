package com.ltf.loanmanagement.entity;

public enum LoanStatus {
    PENDING,        // just submitted, awaiting credit check
    UNDER_REVIEW,   // picked up by the async credit-check consumer
    APPROVED,
    REJECTED,
    DISBURSED,
    CLOSED
}
