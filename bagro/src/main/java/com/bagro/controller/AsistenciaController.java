package com.bagro.controller;

import com.bagro.dto.response.AsistenciaResponse;
import com.bagro.service.AsistenciaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public String iniciarJornada(Authentication authentication) {
        return asistenciaService.iniciarJornada(authentication.getName());
    }

    @PostMapping("/finalizar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public String finalizarJornada(Authentication authentication) {
        return asistenciaService.finalizarJornada(authentication.getName());
    }

    @GetMapping("/mi-estado")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public AsistenciaResponse miEstado(Authentication authentication) {
        return asistenciaService.miEstado(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<AsistenciaResponse> listarAsistencias() {
        return asistenciaService.listarAsistencias();
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<AsistenciaResponse> filtrarAsistenciasPorFecha(@RequestParam String fecha) {
        return asistenciaService.filtrarAsistenciasPorFecha(fecha);
    }
}