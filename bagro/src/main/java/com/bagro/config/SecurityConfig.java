package com.bagro.config;

import com.bagro.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Necesario para que el navegador permita preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // LOGIN / REGISTRO
                        .requestMatchers("/login", "/register").permitAll()

                        // USUARIOS / ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auditorias/**").hasRole("ADMIN")

                        // REPORTES EXCEL / PDF
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "RRHH", "COMPRAS")

                        // MENU
                        .requestMatchers("/api/menu").hasRole("ADMIN")

                        // EMPLEADOS
                        .requestMatchers(HttpMethod.GET, "/api/empleados/mi-perfil").hasRole("TRABAJADOR")
                        .requestMatchers("/api/empleados/**").hasAnyRole("ADMIN", "RRHH")

                        // ASISTENCIAS
                        .requestMatchers(HttpMethod.POST, "/api/asistencia/iniciar").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.POST, "/api/asistencia/finalizar").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.GET, "/api/asistencia/mi-estado").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.GET, "/api/asistencia/**").hasAnyRole("ADMIN", "RRHH")

                        // SOLICITUDES
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/mis-solicitudes").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitudes/**").hasAnyRole("ADMIN", "RRHH")

                        // PAGOS / PLANILLA
                        .requestMatchers("/api/pagos/kpis/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.POST, "/api/pagos").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.PUT, "/api/pagos/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.PATCH, "/api/pagos/**").hasAnyRole("ADMIN", "RRHH")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/trabajador").hasRole("TRABAJADOR")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/**").hasAnyRole("ADMIN", "RRHH")

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        // Necesario para que el frontend pueda leer nombres de archivos PDF/Excel
        configuration.setExposedHeaders(List.of(
                "Content-Disposition"
        ));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}