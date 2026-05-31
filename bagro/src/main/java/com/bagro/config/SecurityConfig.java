package com.bagro.config;

import com.bagro.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                        .requestMatchers("/login", "/register").permitAll()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auditorias/**").hasRole("ADMIN")
                        // MENU
                        .requestMatchers("/api/menu").hasRole("ADMIN")

                        // EMPLEADOS
                        .requestMatchers("/api/empleados/**").hasAnyRole("ADMIN", "RRHH")

                        // PAGOS / PLANILLA
                        .requestMatchers("/api/pagos/kpis/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.POST, "/api/pagos").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.PUT, "/api/pagos/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/trabajador").hasRole("TRABAJADOR")

                        // PROVEEDORES Y COMPRAS
                        .requestMatchers("/api/proveedores/**").hasAnyRole("ADMIN", "COMPRAS")
                        .requestMatchers("/api/compras/**").hasAnyRole("ADMIN", "COMPRAS")

                        // CONSULTAS EXTERNAS
                        .requestMatchers("/api/consultas/dni/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers("/api/consultas/ruc/**").hasAnyRole("ADMIN", "COMPRAS")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}