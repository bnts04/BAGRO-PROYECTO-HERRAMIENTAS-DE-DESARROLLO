package com.bagro.service;

import com.bagro.dto.request.RegisterUserRequest;
import com.bagro.dto.response.UserResponse;
import com.bagro.entity.Role;
import com.bagro.entity.User;
import com.bagro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditoriaService auditoriaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public String createUser(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya existe");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .active(request.isActive())
                .build();

        userRepository.save(user);

        auditoriaService.registrar(
                "USUARIOS",
                "CREAR USUARIO",
                "Se creó el usuario " + user.getUsername()
                        + " con rol " + user.getRole().name()
        );

        return "Usuario creado correctamente";
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        user.isActive()
                ))
                .toList();
    }

    public String changeUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setActive(active);
        userRepository.save(user);

        auditoriaService.registrar(
                "USUARIOS",
                active ? "ACTIVAR USUARIO" : "DESACTIVAR USUARIO",
                "Se " + (active ? "activó" : "desactivó")
                        + " el usuario " + user.getUsername()
                        + " con rol " + user.getRole().name()
        );

        return active ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
    }
}