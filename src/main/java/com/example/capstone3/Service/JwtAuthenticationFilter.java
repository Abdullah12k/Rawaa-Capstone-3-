package com.example.capstone3.Service;

import com.example.capstone3.Config.AuthPrincipal;
import com.example.capstone3.Models.Owner;
import com.example.capstone3.Models.User;
import com.example.capstone3.Repository.OwnerRepository;
import com.example.capstone3.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository users;
    private final OwnerRepository owners;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        AuthPrincipal principal;
        try {
            Jwt jwt = jwtService.validate(header.substring(7));
            String[] subject = jwt.getSubject().split(":", -1);
            if (subject.length != 2) throw new IllegalArgumentException("Invalid subject");
            Integer id = Integer.valueOf(subject[1]);
            String role = subject[0];
            if (!role.equals(jwt.getClaimAsString("role"))) throw new IllegalArgumentException("Invalid role");
            if ("USER".equals(role)) {
                User user = users.findById(id).orElseThrow();
                principal = new AuthPrincipal(role, id, user.getEmail());
            } else if ("OWNER".equals(role)) {
                Owner owner = owners.findById(id).orElseThrow();
                principal = new AuthPrincipal(role, id, owner.getEmail());
            } else throw new IllegalArgumentException("Invalid role");
            if (!principal.email().equalsIgnoreCase(jwt.getClaimAsString("email"))) throw new IllegalArgumentException("Account changed");
        } catch (JwtException | IllegalArgumentException | java.util.NoSuchElementException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid or expired token\"}");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(principal.role()))));
        chain.doFilter(request, response);
    }
}
