package iths.se.connie.authserver.service;

import iths.se.connie.authserver.model.User;
import iths.se.connie.authserver.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String normalizedUsername =
                username.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found in system: "
                                        + normalizedUsername
                        )
                );

        return org.springframework.security.core.userdetails.User

                .withUsername(user.getUsername())

                .password(user.getPassword())

                .roles(user.getRole().name())

                .disabled(!user.isEnabled())

                .build();
    }
}