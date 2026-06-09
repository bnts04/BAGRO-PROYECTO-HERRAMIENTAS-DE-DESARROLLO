package com.bagro.controller;

import com.bagro.dto.request.EmpleadoRequest;
import com.bagro.dto.response.EmpleadoResponse;
import com.bagro.service.EmpleadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.bagro.dto.response.EmpleadoResponse;
import org.springframework.security.core.Authentication;

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String editarEmpleado(@PathVariable Long id, @RequestBody EmpleadoRequest request) {
        return empleadoService.editarEmpleado(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String desactivarEmpleado(@PathVariable Long id) {
        return empleadoService.desactivarEmpleado(id);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String activarEmpleado(@PathVariable Long id) {
        return empleadoService.activarEmpleado(id);
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<EmpleadoResponse> filtrarEmpleadosPorEstado(@RequestParam boolean activo) {
        return empleadoService.filtrarEmpleadosPorEstado(activo);
    }

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public EmpleadoResponse miPerfil(Authentication authentication) {
        return empleadoService.miPerfil(authentication.getName());
    }
}