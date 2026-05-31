package com.bagro.controller;

import com.bagro.dto.request.CompraRequest;
import com.bagro.dto.response.CompraKpiResponse;
import com.bagro.dto.response.CompraResponse;
import com.bagro.service.CompraPdfService;
import com.bagro.service.CompraService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;
    private final CompraPdfService compraPdfService;

    public CompraController(CompraService compraService, CompraPdfService compraPdfService) {
        this.compraService = compraService;
        this.compraPdfService = compraPdfService;
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

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public List<CompraResponse> filtrarComprasPorFecha(
            @RequestParam String desde,
            @RequestParam String hasta
    ) {
        return compraService.filtrarComprasPorFecha(
                LocalDate.parse(desde),
                LocalDate.parse(hasta)
        );
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public CompraKpiResponse obtenerKpisCompras(
            @RequestParam String desde,
            @RequestParam String hasta
    ) {
        return compraService.obtenerKpisCompras(
                LocalDate.parse(desde),
                LocalDate.parse(hasta)
        );
    }

    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public ResponseEntity<byte[]> generarComprobanteCompra(@PathVariable Long id) {
        byte[] pdf = compraPdfService.generarComprobanteCompra(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante-compra-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public String anularCompra(@PathVariable Long id) {
        return compraService.anularCompra(id);
    }
}