package com.example.capstone3.Config;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Account identified by a validated JWT. Controllers can use this instead of trusting a URL ID. */
public record AuthPrincipal(String role, Integer id, String email) {
    public Integer requireUser(Integer requestedId) { return require("USER", requestedId); }
    public Integer requireOwner(Integer requestedId) { return require("OWNER", requestedId); }

    private Integer require(String expectedRole, Integer requestedId) {
        if (!expectedRole.equals(role) || !id.equals(requestedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return id;
    }
}
