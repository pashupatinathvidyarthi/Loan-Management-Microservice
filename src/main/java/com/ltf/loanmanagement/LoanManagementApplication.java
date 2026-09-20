package com.ltf.loanmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Loan Management Microservice.
 *
 * A banking-platform style capstone built around L&amp;T Finance's core lending lines
 * (Personal, Two-Wheeler, Home, Rural Business, Farm, Gold, SME loans). Demonstrates:
 *  - Spring Boot REST APIs (Customers, Loan Applications, Repayments, KYC Documents)
 *  - PostgreSQL via Spring Data JPA/Hibernate (relational core: Customer -> LoanApplication -> Repayment)
 *  - MongoDB (document modelling for unstructured KYC documents / audit metadata)
 *  - Kafka (async loan-application-submitted event -> mock credit-check consumer)
 *  - Docker / Docker Compose for local orchestration of Postgres, MongoDB, Kafka, and the app
 */
@SpringBootApplication
public class LoanManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanManagementApplication.class, args);
    }
}
