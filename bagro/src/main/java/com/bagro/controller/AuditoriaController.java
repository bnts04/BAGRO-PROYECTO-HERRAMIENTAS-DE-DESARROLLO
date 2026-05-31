package com.bagro.controller;

import com.bagro.dto.response.AuditoriaResponse;
import com.bagro.service.AuditoriaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditoriaResponse> listarAuditorias() {
        return auditoriaService.listarAuditorias();
    }

    @GetMapping("/modulo")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditoriaResponse> filtrarPorModulo(@RequestParam String modulo) {
        return auditoriaService.filtrarPorModulo(modulo);
    }

    @GetMapping("/usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditoriaResponse> filtrarPorUsuario(@RequestParam String usuario) {
        return auditoriaService.filtrarPorUsuario(usuario);
    }
}