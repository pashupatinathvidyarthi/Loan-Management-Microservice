package com.ltf.loanmanagement.controller;

import com.ltf.loanmanagement.dto.RepaymentResponse;
import com.ltf.loanmanagement.service.RepaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Repayments", description = "EMI repayment schedules")
public class RepaymentController {

    private final RepaymentService repaymentService;

    @GetMapping("/loan-applications/{loanApplicationId}/repayments")
    public List<RepaymentResponse> getSchedule(@PathVariable Long loanApplicationId) {
        return repaymentService.getScheduleForLoan(loanApplicationId);
    }

    @PatchMapping("/repayments/{repaymentId}/pay")
    public RepaymentResponse markPaid(@PathVariable Long repaymentId) {
        return repaymentService.markPaid(repaymentId);
    }
}
