package com.ltf.loanmanagement.controller;

import com.ltf.loanmanagement.document.KycDocument;
import com.ltf.loanmanagement.dto.KycDocumentRequest;
import com.ltf.loanmanagement.service.KycDocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc-documents")
@RequiredArgsConstructor
@Tag(name = "KYC Documents", description = "Unstructured KYC document metadata, stored in MongoDB")
public class KycDocumentController {

    private final KycDocumentService kycDocumentService;

    @PostMapping
    public ResponseEntity<KycDocument> upload(@Valid @RequestBody KycDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kycDocumentService.upload(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KycDocument> getById(@PathVariable String id) {
        return ResponseEntity.ok(kycDocumentService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public List<KycDocument> getByCustomer(@PathVariable Long customerId) {
        return kycDocumentService.getByCustomer(customerId);
    }

    @GetMapping("/loan-application/{loanApplicationId}")
    public List<KycDocument> getByLoanApplication(@PathVariable Long loanApplicationId) {
        return kycDocumentService.getByLoanApplication(loanApplicationId);
    }

    @PatchMapping("/{id}/verification-status")
    public ResponseEntity<KycDocument> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(kycDocumentService.updateVerificationStatus(id, status));
    }
}
