package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({"id", "userId", "apartmentId", "rating", "comment", "createdAt"})
@Data
public class ReviewDTOOut {

    private Integer id;
    private Integer userId;
    private Integer apartmentId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
