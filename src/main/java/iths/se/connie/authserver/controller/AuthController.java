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
//request mapping för att alla endpoints i denna controller ska börja med /auth
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    //request body är en JSON som ska mappas till UserRegisterRequestDTO
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegisterRequestDTO request) {

        UserResponseDTO user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping("/login")
    //request body är en JSON som ska mappas till LoginRequestDTO
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    //endpoint för att hämta jwks
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }

    //endpoint för att hämta information om den inloggade användaren
    @GetMapping("/me")
    public ResponseEntity<SessionResponseDTO> me(
            @AuthenticationPrincipal Jwt jwt) {
    //hämtar ut när token går ut
        Instant expiresAt = jwt.getExpiresAt();
        //beräknar hur många sekunder som är kvar tills token går ut
        long remainingSeconds = expiresAt != null
                ? Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds())
                : 0;
        //returnerar en SessionResponseDTO med information om den inloggade användaren
        return ResponseEntity.ok(new SessionResponseDTO(
                remainingSeconds,
                jwt.getSubject(),
                jwt.getClaimAsStringList("roles")
        ));
    }

}
