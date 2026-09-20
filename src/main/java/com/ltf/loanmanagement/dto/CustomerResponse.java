package com.ltf.loanmanagement.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String panNumber;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
}
