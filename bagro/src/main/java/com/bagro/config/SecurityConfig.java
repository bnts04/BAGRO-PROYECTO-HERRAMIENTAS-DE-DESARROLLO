package com.bagro.config;

import com.bagro.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register").permitAll() // Login y register permitidos
                        .requestMatchers("/api/menu").hasRole("ADMIN") // Solo admin puede acceder al menu
                        .requestMatchers("/api/pagos/kpis/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers("/api/pagos").hasAnyRole("ADMIN", "RRHH") // RRHH y Admin pueden hacer pagos
                        .requestMatchers("/api/pagos/**").hasRole("TRABAJADOR") // Trabajadores pueden ver sus pagos
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}