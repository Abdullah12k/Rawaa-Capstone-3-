package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.MessageDTOIn;
import com.example.capstone3.Service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/get")
    public ResponseEntity<?> getMessages() {
        return ResponseEntity.status(200).body(messageService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getMessage(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(messageService.getMessage(id));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getMessagesByConversationId(@PathVariable Integer conversationId) {
        return ResponseEntity.status(200).body(messageService.getMessagesByConversationId(conversationId));
    }

    @PostMapping("/add/user/{userId}/{ownerId}/{apartmentId}")
    public ResponseEntity<?> addUserMessage(@PathVariable Integer userId, @PathVariable Integer ownerId, @PathVariable Integer apartmentId,
                                            @RequestBody @Valid MessageDTOIn messageDTOIn) {
        messageService.addUserMessage(userId, ownerId, apartmentId, messageDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Message sent successfully"));
    }

    @PostMapping("/add/owner/{ownerId}/{userId}/{apartmentId}")
    public ResponseEntity<?> addOwnerMessage(@PathVariable Integer ownerId, @PathVariable Integer userId, @PathVariable Integer apartmentId,
                                             @RequestBody @Valid MessageDTOIn messageDTOIn) {
        messageService.addOwnerMessage(ownerId, userId, apartmentId, messageDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Message sent successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable Integer id,
                                           @RequestParam Integer senderId,
                                           @RequestParam String senderRole,
                                           @RequestBody @Valid MessageDTOIn messageDTOIn) {
        messageService.updateMessage(id, senderId, senderRole, messageDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Message updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Integer id,
                                           @RequestParam Integer senderId,
                                           @RequestParam String senderRole) {
        messageService.deleteMessage(id, senderId, senderRole);
        return ResponseEntity.status(200).body(new ApiResponse("Message deleted successfully"));
    }
}
