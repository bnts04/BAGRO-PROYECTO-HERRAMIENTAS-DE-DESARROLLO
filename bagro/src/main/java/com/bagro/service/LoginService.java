package com.bagro.service;

import com.bagro.dto.request.LoginRequest;
import com.bagro.dto.request.RegisterUserRequest;
import com.bagro.dto.response.LoginResponse;
import com.bagro.entity.Role;
import com.bagro.entity.User;
import com.bagro.repository.UserRepository;
import com.bagro.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Aquí usamos PasswordEncoder para comparar la contraseña encriptada
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponse(
                "Login correcto",
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }

    public String registerUser(RegisterUserRequest request) {
        // Verificamos si el nombre de usuario ya existe antes de registrar
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Creamos el usuario y encriptamos la contraseña antes de guardarla
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // Contraseña encriptada
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .active(request.isActive())
                .build();

        userRepository.save(user);

        return "Usuario registrado correctamente";
    }
}