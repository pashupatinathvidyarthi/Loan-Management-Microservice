package com.ltf.loanmanagement.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * KYC documents and their verification metadata are unstructured / schema-flexible
 * (different document types carry different fields), so they live in MongoDB rather
 * than the relational core — exactly the "document modelling" the JD calls out,
 * kept deliberately separate from the Postgres Customer/LoanApplication tables.
 */
@Document(collection = "kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    private String id;

    @Indexed
    private Long customerId;

    @Indexed
    private Long loanApplicationId;

    private String documentType;   // e.g. PAN, AADHAAR, INCOME_PROOF, VEHICLE_RC
    private String fileName;
    private String contentType;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED

    // Flexible bag for document-type-specific fields (e.g. PAN number, RC number,
    // gold purity for a Gold Loan) without needing a schema migration per field.
    private Map<String, Object> metadata;

    @CreatedDate
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
