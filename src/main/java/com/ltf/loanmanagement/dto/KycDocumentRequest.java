package com.ltf.loanmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentRequest {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long loanApplicationId;

    @NotBlank(message = "documentType is required")
    private String documentType;

    @NotBlank(message = "fileName is required")
    private String fileName;

    private String contentType;

    private Map<String, Object> metadata;
}
