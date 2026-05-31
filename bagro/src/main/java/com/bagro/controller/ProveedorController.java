package com.bagro.controller;

import com.bagro.dto.request.ProveedorRequest;
import com.bagro.dto.response.ProveedorResponse;
import com.bagro.service.ProveedorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public String crearProveedor(@RequestBody ProveedorRequest request) {
        return proveedorService.crearProveedor(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public List<ProveedorResponse> listarProveedores() {
        return proveedorService.listarProveedores();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public String editarProveedor(@PathVariable Long id, @RequestBody ProveedorRequest request) {
        return proveedorService.editarProveedor(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public String desactivarProveedor(@PathVariable Long id) {
        return proveedorService.desactivarProveedor(id);
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public List<ProveedorResponse> filtrarProveedoresPorEstado(@RequestParam boolean activo) {
        return proveedorService.filtrarProveedoresPorEstado(activo);
    }
}