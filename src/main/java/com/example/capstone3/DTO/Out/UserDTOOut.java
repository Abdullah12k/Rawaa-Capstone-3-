package com.example.capstone3.DTO.Out;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id", "fullName", "email", "phoneNumber", "dateOfBirth",
        "familyCount", "childrenCount", "createdAt"
})
@Data
public class UserDTOOut {

    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private Integer familyCount;
    private Integer childrenCount;
    private LocalDateTime createdAt;
}
