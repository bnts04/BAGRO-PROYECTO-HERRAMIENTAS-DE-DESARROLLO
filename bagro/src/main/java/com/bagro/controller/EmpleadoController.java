package com.bagro.controller;

import com.bagro.dto.request.EmpleadoRequest;
import com.bagro.dto.response.EmpleadoResponse;
import com.bagro.service.EmpleadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String crearEmpleado(@RequestBody EmpleadoRequest request) {
        return empleadoService.crearEmpleado(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<EmpleadoResponse> listarEmpleados() {
        return empleadoService.listarEmpleados();
    }
}