package iths.se.connie.authserver.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import iths.se.connie.authserver.dto.LoginRequestDTO;
import iths.se.connie.authserver.dto.TokenResponseDTO;
import iths.se.connie.authserver.dto.UserRegisterRequestDTO;
import iths.se.connie.authserver.dto.UserResponseDTO;
import iths.se.connie.authserver.model.Role;
import iths.se.connie.authserver.model.User;
import iths.se.connie.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// Service class för autentisering och hantering av användare
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final KeyPair keyPair;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // JWT configuration properties
    private final String jwtIssuer;
    private final String jwtKeyId;
    private final long jwtExpirationMinutes;
    // Konstruktor för AuthService som tar emot beroenden och konfigurationsvärden
    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            KeyPair keyPair,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,

            // JWT configuration properties
            @Value("${app.jwt.issuer}") String jwtIssuer,
            @Value("${app.jwt.key-id}") String jwtKeyId,
            @Value("${app.jwt.expiration-minutes}") long jwtExpirationMinutes
    ) {
        // Beroenden och konfigurationsvärden tilldelas till fält
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.keyPair = keyPair;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.jwtIssuer = jwtIssuer;
        this.jwtKeyId = jwtKeyId;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
    }

    // Metod för att registrera en ny användare
    public UserResponseDTO register(UserRegisterRequestDTO request) {

        String normalizedUsername =
                request.username()
                        .trim()
                        .toLowerCase(Locale.ROOT);
        // Kontrollera om användarnamnet redan finns i databasen
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }
        // Skapa en ny användare och sätt dess egenskaper
        User user = new User();

        user.setUsername(normalizedUsername);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setRole(
                request.role() == null || request.role().isBlank()
                        ? Role.USER
                        : Role.valueOf(request.role().trim().toUpperCase(Locale.ROOT))
        );

        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername()
        );
    }
    // Metod för att logga in en användare och generera en JWT-token
    public TokenResponseDTO login(LoginRequestDTO request) {

        // Normalisera användarnamnet genom att ta bort mellanslag och göra stora bokstäver till små
        String normalizedUsername =
                request.username()
                        .trim()
                        .toLowerCase(Locale.ROOT);
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                normalizedUsername,
                                request.password()
                        )
                );
        // Hämta användarens detaljer och roller från autentiseringsobjektet
        UserDetails principal =
                (UserDetails) authentication.getPrincipal();

        List<String> roles =
                authentication.getAuthorities().stream()

                        .map(grantedAuthority ->
                                grantedAuthority.getAuthority()
                        )

                        .filter(authority ->
                                authority.startsWith("ROLE_")
                        )

                        .map(authority ->
                                authority.replace("ROLE_", "")
                        )

                        .toList();
        Instant now = Instant.now();
        // Beräkna när token ska gå ut baserat på nuvarande tid och konfigurerad expiration tid
        Instant expiresAt =
                now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES);
        // Skapa JWT claims med information om issuer, issuedAt, expiresAt, subject och roller
        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(jwtIssuer)

                        .issuedAt(now)

                        .expiresAt(expiresAt)

                        .subject(principal.getUsername())

                        .claim("roles", roles)

                        .build();
        //
        JwsHeader jwsHeader =
                JwsHeader.with(SignatureAlgorithm.RS256)

                        .keyId(jwtKeyId)

                        .build();
        //Kryptera jwt med hjälp av jwtEncoder
        String accessToken =
                jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                jwsHeader,
                                claims
                        )
                ).getTokenValue();
        //
        return new TokenResponseDTO(
                accessToken,

                ChronoUnit.SECONDS.between(now, expiresAt),

                principal.getUsername(),

                roles
        );
    }
    // Metod för att hämta den publika JWK-setet som används för att verifiera JWT-signaturen
    public Map<String, Object> publicJwkSet() {

        RSAKey rsaKey =
                new RSAKey.Builder(
                        (RSAPublicKey) keyPair.getPublic()
                )
                        .keyID(jwtKeyId)
                        .build();

        return new JWKSet(rsaKey).toJSONObject();
    }
}