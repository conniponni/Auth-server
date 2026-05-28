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

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final KeyPair keyPair;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String jwtIssuer;
    private final String jwtKeyId;
    private final long jwtExpirationMinutes;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            KeyPair keyPair,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,

            @Value("${app.jwt.issuer}") String jwtIssuer,
            @Value("${app.jwt.key-id}") String jwtKeyId,
            @Value("${app.jwt.expiration-minutes}") long jwtExpirationMinutes
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.keyPair = keyPair;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.jwtIssuer = jwtIssuer;
        this.jwtKeyId = jwtKeyId;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
    }

    public UserResponseDTO register(UserRegisterRequestDTO request) {

        String normalizedUsername =
                request.username()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setUsername(normalizedUsername);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setRole(Role.USER);

        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername()
        );
    }

    public TokenResponseDTO login(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        UserDetails principal =
                (UserDetails) authentication.getPrincipal();

        List<String> roles =
                authentication.getAuthorities().stream()

                        .map(grantedAuthority ->
                                grantedAuthority.getAuthority()
                                        .replace("ROLE_", "")
                        )

                        .toList();

        Instant now = Instant.now();

        Instant expiresAt =
                now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(jwtIssuer)

                        .issuedAt(now)

                        .expiresAt(expiresAt)

                        .subject(principal.getUsername())

                        .claim("roles", roles)

                        .build();

        JwsHeader jwsHeader =
                JwsHeader.with(SignatureAlgorithm.RS256)

                        .keyId(jwtKeyId)

                        .build();

        String accessToken =
                jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                jwsHeader,
                                claims
                        )
                ).getTokenValue();

        return new TokenResponseDTO(
                accessToken,

                ChronoUnit.SECONDS.between(now, expiresAt),

                principal.getUsername(),

                roles
        );
    }

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