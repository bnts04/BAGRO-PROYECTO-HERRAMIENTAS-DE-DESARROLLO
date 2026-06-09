package com.bagro.controller;

import com.bagro.dto.request.PagoRequest;
import com.bagro.dto.response.PagoResponse;
import com.bagro.dto.response.PlanillaKpiResponse;
import com.bagro.service.PagoPdfService;
import com.bagro.service.PagoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final PagoPdfService pagoPdfService;

    public PagoController(PagoService pagoService, PagoPdfService pagoPdfService) {
        this.pagoService = pagoService;
        this.pagoPdfService = pagoPdfService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String crearPago(@RequestBody PagoRequest request, Authentication authentication) {
        return pagoService.crearPago(authentication.getName(), request);
    }

    @GetMapping("/trabajador")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public List<PagoResponse> listarPagos(Authentication authentication) {
        return pagoService.listarPagos(authentication.getName());
    }

    @GetMapping("/kpis/planilla")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public PlanillaKpiResponse obtenerKpisPlanilla(
            @RequestParam int mes,
            @RequestParam int anio
    ) {
        return pagoService.obtenerKpisPlanilla(mes, anio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String editarPago(@PathVariable Long id, @RequestBody PagoRequest request) {
        return pagoService.editarPago(id, request);
    }

    @GetMapping("/{id}/boleta")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH','TRABAJADOR')")
    public ResponseEntity<byte[]> generarBoletaPago(@PathVariable Long id) {
        byte[] pdf = pagoPdfService.generarBoletaPago(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=boleta-pago-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public List<PagoResponse> filtrarPagosPorMesAnio(
            @RequestParam Integer mes,
            @RequestParam Integer anio
    ) {
        return pagoService.filtrarPagosPorMesAnio(mes, anio);
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public String anularPago(@PathVariable Long id) {
        return pagoService.anularPago(id);
    }
}