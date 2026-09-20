package com.ltf.loanmanagement.controller;

import com.ltf.loanmanagement.dto.LoanApplicationRequest;
import com.ltf.loanmanagement.dto.LoanApplicationResponse;
import com.ltf.loanmanagement.entity.LoanStatus;
import com.ltf.loanmanagement.entity.LoanType;
import com.ltf.loanmanagement.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Applications", description = "Submit and track loan applications across all L&T Finance product lines")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    @Operation(summary = "Submit a new loan application",
            description = "Persists the application, generates its EMI schedule, and publishes a " +
                    "LoanApplicationSubmitted Kafka event for async credit-check processing.")
    public ResponseEntity<LoanApplicationResponse> submit(@Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanApplicationService.submit(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanApplicationService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List loan applications (paginated, optional status/type filters)")
    public ResponseEntity<Page<LoanApplicationResponse>> list(
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) LoanType type,
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable) {

        if (status != null) {
            return ResponseEntity.ok(loanApplicationService.listByStatus(status, pageable));
        }
        if (type != null) {
            return ResponseEntity.ok(loanApplicationService.listByType(type, pageable));
        }
        return ResponseEntity.ok(loanApplicationService.list(pageable));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<LoanApplicationResponse>> listByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "appliedAt") Pageable pageable) {
        return ResponseEntity.ok(loanApplicationService.listByCustomer(customerId, pageable));
    }
}
