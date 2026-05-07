package com.bagro.controller;

import com.bagro.dto.request.PagoRequest;
import com.bagro.dto.response.PagoResponse;
import com.bagro.service.PagoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String crearPago(@RequestBody PagoRequest request, @RequestParam String username) {
        return pagoService.crearPago(username, request);
    }

    @GetMapping("/trabajador")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public List<PagoResponse> listarPagos(@RequestParam String username) {
        return pagoService.listarPagos(username);
    }
}