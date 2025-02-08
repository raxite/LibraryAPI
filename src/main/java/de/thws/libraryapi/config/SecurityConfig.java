package de.thws.libraryapi.config;

import de.thws.libraryapi.domain.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig
{

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // Frames für H2 erlauben
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/register").permitAll() //registrierung für alle erlauben
                        .requestMatchers("/h2-console/**").permitAll() // H2-Konsole öffentlich zugänglich machen
                        .requestMatchers("/users/**").hasAnyRole("ADMIN" , "USER")  // Admin can manage users
                        .requestMatchers("/books/**").hasAnyRole("ADMIN", "USER")  // Both can view books
                        .requestMatchers("/authors/**").permitAll()  // Public endpoints (login, register)
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()); //  Use withDefaults() instead of deprecated .httpBasic()

        return http.build();
    }
}
