package com.example.capstone3.DTO.Out;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "id", "conversationId", "senderId", "senderRole", "content", "sentAt"
})
@Data
public class MessageDTOOut {

    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String senderRole;
    private String content;
    private LocalDateTime sentAt;
}
