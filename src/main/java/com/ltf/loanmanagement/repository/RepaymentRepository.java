package com.ltf.loanmanagement.repository;

import com.ltf.loanmanagement.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanApplicationIdOrderByInstallmentNumberAsc(Long loanApplicationId);
}
