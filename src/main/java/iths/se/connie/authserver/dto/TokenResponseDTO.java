package iths.se.connie.authserver.dto;

import java.util.List;

public record TokenResponseDTO(
        String token,
        long expiresIn,
        String subject,
        List<String> roles
) {
}
