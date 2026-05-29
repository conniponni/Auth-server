package iths.se.connie.authserver.controller;


import iths.se.connie.authserver.dto.LoginRequestDTO;
import iths.se.connie.authserver.dto.TokenResponseDTO;
import iths.se.connie.authserver.dto.UserRegisterRequestDTO;
import iths.se.connie.authserver.dto.UserResponseDTO;
import iths.se.connie.authserver.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping ("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok(authService.publicJwkSet());
    }

    @GetMapping("/me")
    public Authentication me(Authentication authentication) {
        return authentication;
    }
}
