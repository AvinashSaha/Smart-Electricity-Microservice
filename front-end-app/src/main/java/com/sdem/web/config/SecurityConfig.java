package com.sdem.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Authentication and authorisation.
 *
 * <ul>
 *     <li>Users live in an in-memory store ({@link InMemoryUserDetailsManager}); passwords are BCrypt hashed.</li>
 *     <li>Every page needs a signed in user; user management needs the ADMIN role.</li>
 *     <li>Form login with CSRF protection, session fixation protection and logout.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** In-memory user store, seeded from sdem.frontend.users. */
    @Bean
    public InMemoryUserDetailsManager userDetailsManager(FrontEndProperties properties, PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        if (properties.users() != null) {
            for (FrontEndProperties.UserSeed seed : properties.users()) {
                manager.createUser(User.withUsername(seed.username())
                        .password(encoder.encode(seed.password()))
                        .roles(seed.role())
                        .build());
            }
        }
        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/actuator/health", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:")));
        return http.build();
    }
}
