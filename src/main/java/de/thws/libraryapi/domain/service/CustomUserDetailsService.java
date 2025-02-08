package de.thws.libraryapi.domain.service;


import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 🔥 Spring Security User-Objekt erstellen
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        builder.username(user.getUsername());
        builder.password(user.getPassword()); // Das verschlüsselte Passwort aus der DB verwenden
        builder.roles(user.getRole().name()); // Rolle aus DB setzen

        return builder.build();
    }
}
