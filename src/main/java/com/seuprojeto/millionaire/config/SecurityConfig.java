package com.seuprojeto.millionaire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Para simplificar neste projeto educacional
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permitir tudo para simplificar
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Permitir H2 Console
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            );
        
        return http.build();
    }
}

