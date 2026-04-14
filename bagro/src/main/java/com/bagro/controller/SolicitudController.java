package com.bagro.controller;

import com.bagro.dto.request.SolicitudRequest;
import com.bagro.dto.request.SolicitudRevisionRequest;
import com.bagro.dto.response.SolicitudResponse;
import com.bagro.service.SolicitudService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public String crearSolicitud(Authentication authentication,
                                 @RequestBody SolicitudRequest request) {
        return solicitudService.crearSolicitud(authentication.getName(), request);
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public List<SolicitudResponse> misSolicitudes(Authentication authentication) {
        return solicitudService.misSolicitudes(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<SolicitudResponse> listarSolicitudes() {
        return solicitudService.listarSolicitudes();
    }

    @PatchMapping("/{id}/revision")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String revisarSolicitud(@PathVariable Long id,
                                   @RequestBody SolicitudRevisionRequest request) {
        return solicitudService.revisarSolicitud(id, request);
    }
}