package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.ConversationDTOIn;
import com.example.capstone3.Service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/get")
    public ResponseEntity<?> getConversations() {
        return ResponseEntity.status(200).body(conversationService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getConversation(@PathVariable Integer id) {
        return ResponseEntity.status(200).body(conversationService.getConversation(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getConversationsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(conversationService.getConversationsByUserId(userId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getConversationsByOwnerId(@PathVariable Integer ownerId) {
        return ResponseEntity.status(200).body(conversationService.getConversationsByOwnerId(ownerId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addConversation(@RequestBody @Valid ConversationDTOIn conversationDTOIn) {
        conversationService.addConversation(conversationDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Conversation added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateConversation(@PathVariable Integer id, @RequestBody @Valid ConversationDTOIn conversationDTOIn) {
        conversationService.updateConversation(id, conversationDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("Conversation updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteConversation(@PathVariable Integer id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.status(200).body(new ApiResponse("Conversation deleted successfully"));
    }
}
