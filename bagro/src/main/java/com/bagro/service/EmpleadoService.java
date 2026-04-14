package com.bagro.service;

import com.bagro.dto.request.EmpleadoRequest;
import com.bagro.dto.response.EmpleadoResponse;
import com.bagro.entity.Empleado;
import com.bagro.entity.Role;
import com.bagro.entity.User;
import com.bagro.repository.EmpleadoRepository;
import com.bagro.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String crearEmpleado(EmpleadoRequest request) {
        if (empleadoRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("Ya existe un empleado con ese DNI");
        }

        if (userRepository.existsByUsername(request.getDni())) {
            throw new RuntimeException("Ya existe un usuario con ese DNI");
        }

        User user = User.builder()
                .username(request.getDni())
                .password(passwordEncoder.encode(request.getDni()))
                .role(Role.TRABAJADOR)
                .active(true)
                .build();

        userRepository.save(user);

        Empleado empleado = Empleado.builder()
                .dni(request.getDni())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cargo(request.getCargo())
                .area(request.getArea())
                .sueldoBase(request.getSueldoBase())
                .activo(true)
                .user(user)
                .build();

        empleadoRepository.save(empleado);

        return "Empleado y usuario creados correctamente";
    }

    public List<EmpleadoResponse> listarEmpleados() {
        return empleadoRepository.findAll()
                .stream()
                .map(e -> new EmpleadoResponse(
                        e.getId(),
                        e.getDni(),
                        e.getNombres(),
                        e.getApellidos(),
                        e.getCargo(),
                        e.getArea(),
                        e.getSueldoBase(),
                        e.isActivo(),
                        e.getUser() != null ? e.getUser().getUsername() : null
                ))
                .toList();
    }
}