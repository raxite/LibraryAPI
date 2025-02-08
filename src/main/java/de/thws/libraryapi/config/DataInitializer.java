package de.thws.libraryapi.config;

import de.thws.libraryapi.domain.model.Role;
import de.thws.libraryapi.domain.model.User;
import de.thws.libraryapi.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.Optional;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initialize() {
        createRootAdmin();
    }

    private void createRootAdmin() {
        String rootAdminUsername = "rootAdmin";
        Optional<User> existingAdmin = userRepository.findByUsername(rootAdminUsername);

        if (existingAdmin.isEmpty()) {
            User rootAdmin = new User();
            rootAdmin.setName("Root Admin");
            rootAdmin.setUsername(rootAdminUsername);
            rootAdmin.setPassword(passwordEncoder.encode("root123"));
            rootAdmin.setRole(Role.ADMIN);
            rootAdmin.setBorrowLimit(100); // Admins können viele Bücher verwalten

            userRepository.save(rootAdmin);
            System.out.println(" Root Admin wurde erstellt: rootAdmin / root123");
        } else {
            System.out.println(" Root Admin existiert bereits.");
        }
    }
}