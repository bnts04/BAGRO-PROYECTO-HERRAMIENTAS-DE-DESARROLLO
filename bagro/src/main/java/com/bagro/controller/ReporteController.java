package com.bagro.controller;

import com.bagro.service.ReporteExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteExcelService reporteExcelService;

    public ReporteController(ReporteExcelService reporteExcelService) {
        this.reporteExcelService = reporteExcelService;
    }

    @GetMapping("/trabajadores/excel")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ResponseEntity<byte[]> exportarTrabajadoresExcel() {
        byte[] excel = reporteExcelService.generarReporteTrabajadores();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-trabajadores-bagro.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/pagos/excel")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ResponseEntity<byte[]> exportarPagosExcel(@RequestParam Integer mes,
                                                     @RequestParam Integer anio) {
        byte[] excel = reporteExcelService.generarReportePagos(mes, anio);

        String filename = "reporte-pagos-bagro-" + mes + "-" + anio + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/asistencias/excel")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ResponseEntity<byte[]> exportarAsistenciasExcel(@RequestParam String desde,
                                                           @RequestParam String hasta) {
        LocalDate fechaDesde = LocalDate.parse(desde);
        LocalDate fechaHasta = LocalDate.parse(hasta);

        byte[] excel = reporteExcelService.generarReporteAsistencias(fechaDesde, fechaHasta);

        String filename = "reporte-asistencias-bagro-" + desde + "-al-" + hasta + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/solicitudes/excel")
    @PreAuthorize("hasAnyRole('ADMIN','RRHH')")
    public ResponseEntity<byte[]> exportarSolicitudesExcel() {
        byte[] excel = reporteExcelService.generarReporteSolicitudes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-solicitudes-bagro.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/compras/excel")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
    public ResponseEntity<byte[]> exportarComprasExcel(@RequestParam String desde,
                                                       @RequestParam String hasta) {
        LocalDate fechaDesde = LocalDate.parse(desde);
        LocalDate fechaHasta = LocalDate.parse(hasta);

        byte[] excel = reporteExcelService.generarReporteCompras(fechaDesde, fechaHasta);

        String filename = "reporte-compras-bagro-" + desde + "-al-" + hasta + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}