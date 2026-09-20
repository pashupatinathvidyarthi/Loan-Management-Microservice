package com.ltf.loanmanagement.service;

import com.ltf.loanmanagement.dto.RepaymentResponse;
import com.ltf.loanmanagement.entity.Repayment;
import com.ltf.loanmanagement.exception.ResourceNotFoundException;
import com.ltf.loanmanagement.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getScheduleForLoan(Long loanApplicationId) {
        return repaymentRepository.findByLoanApplicationIdOrderByInstallmentNumberAsc(loanApplicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RepaymentResponse markPaid(Long repaymentId) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment " + repaymentId + " not found"));
        repayment.setPaid(true);
        repayment.setPaidDate(LocalDate.now());
        return toResponse(repaymentRepository.save(repayment));
    }

    private RepaymentResponse toResponse(Repayment r) {
        return RepaymentResponse.builder()
                .id(r.getId())
                .installmentNumber(r.getInstallmentNumber())
                .dueDate(r.getDueDate())
                .emiAmount(r.getEmiAmount())
                .principalComponent(r.getPrincipalComponent())
                .interestComponent(r.getInterestComponent())
                .paid(r.getPaid())
                .paidDate(r.getPaidDate())
                .build();
    }
}
