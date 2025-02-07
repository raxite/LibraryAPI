package de.thws.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults; // ✅ Import this

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/**").hasRole("ADMIN")  // Admin can manage users
                        .requestMatchers("/books/**").hasAnyRole("ADMIN", "USER")  // Both can view books
                        .requestMatchers("/auth/**").permitAll()  // Public endpoints (login, register)
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()); //  Use withDefaults() instead of deprecated .httpBasic()

        return http.build();
    }
}
