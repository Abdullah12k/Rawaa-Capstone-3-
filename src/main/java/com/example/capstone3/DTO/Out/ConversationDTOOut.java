package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({"id", "userId", "ownerId", "apartmentId", "createdAt", "messages"})
@Data
public class ConversationDTOOut {

    private Integer id;
    private Integer userId;
    private Integer ownerId;
    private Integer apartmentId;
    private LocalDateTime createdAt;
    private List<MessageDTOOut> messages;
}
