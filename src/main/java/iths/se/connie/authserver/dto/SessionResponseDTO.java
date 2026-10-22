package iths.se.connie.authserver.dto;

import java.util.List;

public record SessionResponseDTO(
        Long expiresIn,
        String subject,
        List<String> roles
) {
}
