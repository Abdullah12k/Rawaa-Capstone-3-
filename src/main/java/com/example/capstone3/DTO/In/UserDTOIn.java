package com.example.capstone3.DTO.In;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTOIn {

    @NotEmpty(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotEmpty(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "Gender must be exactly MALE or FEMALE")
    private String gender;

    @Min(value = 0, message = "Family count cannot be negative")
    private Integer familyCount;

    @NotNull(message = "Children count is required")
    @Min(value = 0, message = "Children count cannot be negative")
    private Integer childrenCount;
}
