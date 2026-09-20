package com.ltf.loanmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "fullName is required")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @NotBlank(message = "panNumber is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "panNumber must match PAN format e.g. ABCDE1234F")
    private String panNumber;

    @NotNull(message = "dateOfBirth is required")
    @Past(message = "dateOfBirth must be in the past")
    private LocalDate dateOfBirth;
}
