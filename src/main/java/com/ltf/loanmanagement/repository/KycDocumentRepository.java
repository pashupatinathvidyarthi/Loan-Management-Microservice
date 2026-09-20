package com.ltf.loanmanagement.repository;

import com.ltf.loanmanagement.document.KycDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface KycDocumentRepository extends MongoRepository<KycDocument, String> {
    List<KycDocument> findByCustomerId(Long customerId);
    List<KycDocument> findByLoanApplicationId(Long loanApplicationId);
}
