package iths.se.connie.authserver.controller;


import iths.se.connie.authserver.dto.*;
import iths.se.connie.authserver.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegisterRequestDTO request) {

        UserResponseDTO user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }


    @GetMapping("/me")
    public ResponseEntity<SessionResponseDTO> me(
            @AuthenticationPrincipal Jwt jwt) {

        Instant expiresAt = jwt.getExpiresAt();

        long remainingSeconds = expiresAt != null
                ? Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds())
                : 0;

        return ResponseEntity.ok(new SessionResponseDTO(
                remainingSeconds,
                jwt.getSubject(),
                jwt.getClaimAsStringList("roles")
        ));
    }

}
