package com.example.capstone3.Controller;

import com.example.capstone3.DTO.In.OwnerDTOIn;
import com.example.capstone3.DTO.In.UserDTOIn;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.User;
import com.example.capstone3.Config.AuthPrincipal;
import com.example.capstone3.Service.JwtService;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users;
    private final OwnerRepository owners;
    private final PasswordEncoder passwords;
    private final JwtService jwtService;

    public AuthController(UserRepository users, OwnerRepository owners, PasswordEncoder passwords,
                          JwtService jwtService) {
        this.users = users; this.owners = owners; this.passwords = passwords;
        this.jwtService = jwtService;
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserRegistration(@Valid UserDTOIn profile, @NotBlank @Size(min = 8) String password) {}
    public record OwnerRegistration(@Valid OwnerDTOIn profile, @NotBlank @Size(min = 8) String password) {}
    public record AuthResponse(String token, String role, Integer id, String fullName) {}

    @PostMapping("/register/user")
    public AuthResponse registerUser(@Valid @RequestBody UserRegistration request) {
        UserDTOIn p = request.profile();
        requireUniqueEmail(p.getEmail());
        User user = new User();
        user.setFullName(p.getFullName()); user.setEmail(p.getEmail().trim().toLowerCase());
        user.setPhoneNumber(p.getPhoneNumber()); user.setGender(p.getGender());
        user.setDateOfBirth(p.getDateOfBirth()); user.setFamilyCount(p.getFamilyCount());
        user.setChildrenCount(p.getChildrenCount()); user.setPasswordHash(passwords.encode(request.password()));
        users.save(user);
        return token("USER", user.getId(), user.getFullName());
    }

    @PostMapping("/register/owner")
    public AuthResponse registerOwner(@Valid @RequestBody OwnerRegistration request) {
        OwnerDTOIn p = request.profile();
        requireUniqueEmail(p.getEmail());
        Owner owner = new Owner();
        owner.setFullName(p.getFullName()); owner.setEmail(p.getEmail().trim().toLowerCase());
        owner.setPhoneNumber(p.getPhoneNumber());
        owner.setCommercialRegistrationNumber(p.getCommercialRegistrationNumber());
        owner.setPasswordHash(passwords.encode(request.password()));
        owners.save(owner);
        return token("OWNER", owner.getId(), owner.getFullName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email());
        if (user != null && user.getPasswordHash() != null && passwords.matches(request.password(), user.getPasswordHash()))
            return token("USER", user.getId(), user.getFullName());
        Owner owner = owners.findByEmailIgnoreCase(request.email());
        if (owner != null && owner.getPasswordHash() != null && passwords.matches(request.password(), owner.getPasswordHash()))
            return token("OWNER", owner.getId(), owner.getFullName());
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private void requireUniqueEmail(String email) {
        if (users.findByEmailIgnoreCase(email) != null || owners.findByEmailIgnoreCase(email) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }

    private AuthResponse token(String role, Integer id, String name) {
        String email = "USER".equals(role) ? users.findById(id).orElseThrow().getEmail() : owners.findById(id).orElseThrow().getEmail();
        String value = jwtService.generateToken(new AuthPrincipal(role, id, email), name);
        return new AuthResponse(value, role, id, name);
    }
}
