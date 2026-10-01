package iths.se.connie.authserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record UserRegisterRequestDTO(

        @Email(message = "Email must be valid")
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Role is required")
        @DefaultValue("USER")
        String role,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}