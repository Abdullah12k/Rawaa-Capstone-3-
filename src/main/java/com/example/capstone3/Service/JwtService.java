package com.example.capstone3.Service;

import com.example.capstone3.Config.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    @Value("${app.jwt.issuer}") private String issuer;
    @Value("${app.jwt.ttl-hours}") private long ttlHours;

    public String generateToken(AuthPrincipal account, String name) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now)
                .expiresAt(now.plus(ttlHours, ChronoUnit.HOURS))
                .subject(account.role() + ":" + account.id())
                .claim("role", account.role()).claim("name", name)
                .claim("email", account.email()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt validate(String token) { return decoder.decode(token); }
}
