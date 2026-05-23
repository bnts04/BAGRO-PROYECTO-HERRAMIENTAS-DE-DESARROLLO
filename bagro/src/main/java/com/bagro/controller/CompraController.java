package com.bagro.controller;

import com.bagro.dto.request.CompraRequest;
import com.bagro.dto.response.CompraResponse;
import com.bagro.service.CompraService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public String crearCompra(@RequestBody CompraRequest request) {
        return compraService.crearCompra(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public List<CompraResponse> listarCompras() {
        return compraService.listarCompras();
    }
}