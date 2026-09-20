package com.ltf.loanmanagement.service;

import com.ltf.loanmanagement.dto.LoanApplicationRequest;
import com.ltf.loanmanagement.dto.LoanApplicationResponse;
import com.ltf.loanmanagement.entity.Customer;
import com.ltf.loanmanagement.entity.LoanApplication;
import com.ltf.loanmanagement.entity.LoanStatus;
import com.ltf.loanmanagement.entity.LoanType;
import com.ltf.loanmanagement.exception.ResourceNotFoundException;
import com.ltf.loanmanagement.kafka.LoanApplicationEventProducer;
import com.ltf.loanmanagement.repository.CustomerRepository;
import com.ltf.loanmanagement.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final EmiCalculatorService emiCalculatorService;
    private final LoanApplicationEventProducer loanApplicationEventProducer;

    public LoanApplicationResponse submit(LoanApplicationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + request.getCustomerId() + " not found"));

        LoanApplication loan = LoanApplication.builder()
                .customer(customer)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRatePercent(request.getInterestRatePercent())
                .tenureMonths(request.getTenureMonths())
                .status(LoanStatus.PENDING)
                .build();

        // Generate the full EMI schedule up front so it's ready the moment the loan is approved.
        loan.getRepayments().addAll(emiCalculatorService.generateSchedule(loan));

        LoanApplication saved = loanApplicationRepository.save(loan);

        // Publish only after the DB commit succeeds, so we never emit a Kafka event for a
        // loan application that ultimately rolled back (e.g. a later validation failure in
        // the same transaction). registerSynchronization is stateless per-call - safe on a
        // singleton service, unlike stashing the loan in an instance field would be.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                loanApplicationEventProducer.publishLoanApplicationSubmitted(saved);
            }
        });

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> list(Pageable pageable) {
        return loanApplicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> listByStatus(LoanStatus status, Pageable pageable) {
        return loanApplicationRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> listByType(LoanType type, Pageable pageable) {
        return loanApplicationRepository.findByLoanType(type, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> listByCustomer(Long customerId, Pageable pageable) {
        return loanApplicationRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
    }

    private LoanApplication findOrThrow(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication " + id + " not found"));
    }

    private LoanApplicationResponse toResponse(LoanApplication l) {
        return LoanApplicationResponse.builder()
                .id(l.getId())
                .customerId(l.getCustomer().getId())
                .customerName(l.getCustomer().getFullName())
                .loanType(l.getLoanType())
                .principalAmount(l.getPrincipalAmount())
                .interestRatePercent(l.getInterestRatePercent())
                .tenureMonths(l.getTenureMonths())
                .status(l.getStatus())
                .appliedAt(l.getAppliedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
