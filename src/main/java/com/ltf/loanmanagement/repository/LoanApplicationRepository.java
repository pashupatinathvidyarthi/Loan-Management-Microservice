package com.ltf.loanmanagement.repository;

import com.ltf.loanmanagement.entity.LoanApplication;
import com.ltf.loanmanagement.entity.LoanStatus;
import com.ltf.loanmanagement.entity.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Page<LoanApplication> findByStatus(LoanStatus status, Pageable pageable);
    Page<LoanApplication> findByLoanType(LoanType loanType, Pageable pageable);
    Page<LoanApplication> findByCustomerId(Long customerId, Pageable pageable);
}
