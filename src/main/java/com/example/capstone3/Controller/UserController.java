package com.example.capstone3.Controller;

import com.example.capstone3.Api.ApiResponse;
import com.example.capstone3.DTO.In.UserDTOIn;
import com.example.capstone3.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.capstone3.Config.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.status(200).body(userService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireSelf(principal, id);
        return ResponseEntity.status(200).body(userService.getUser(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody @Valid UserDTOIn userDTOIn, @AuthenticationPrincipal AuthPrincipal principal) {
        requireSelf(principal, id);
        userService.updateUser(id, userDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("User updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id, @AuthenticationPrincipal AuthPrincipal principal) {
        requireSelf(principal, id);
        userService.deleteUser(id);
        return ResponseEntity.status(200).body(new ApiResponse("User deleted successfully"));
    }

    private void requireSelf(AuthPrincipal principal, Integer id) {
        if (!principal.role().equals("USER") || !principal.id().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
