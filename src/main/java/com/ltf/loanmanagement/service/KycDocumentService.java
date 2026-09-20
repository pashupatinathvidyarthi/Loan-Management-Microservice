package com.ltf.loanmanagement.service;

import com.ltf.loanmanagement.document.KycDocument;
import com.ltf.loanmanagement.dto.KycDocumentRequest;
import com.ltf.loanmanagement.exception.ResourceNotFoundException;
import com.ltf.loanmanagement.repository.KycDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KycDocumentService {

    private final KycDocumentRepository kycDocumentRepository;

    public KycDocument upload(KycDocumentRequest request) {
        KycDocument document = KycDocument.builder()
                .customerId(request.getCustomerId())
                .loanApplicationId(request.getLoanApplicationId())
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .verificationStatus("PENDING")
                .metadata(request.getMetadata())
                .build();
        return kycDocumentRepository.save(document);
    }

    public KycDocument getById(String id) {
        return kycDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document " + id + " not found"));
    }

    public List<KycDocument> getByCustomer(Long customerId) {
        return kycDocumentRepository.findByCustomerId(customerId);
    }

    public List<KycDocument> getByLoanApplication(Long loanApplicationId) {
        return kycDocumentRepository.findByLoanApplicationId(loanApplicationId);
    }

    public KycDocument updateVerificationStatus(String id, String status) {
        KycDocument document = getById(id);
        document.setVerificationStatus(status);
        return kycDocumentRepository.save(document);
    }
}
