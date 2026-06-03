package com.bagro.service;

import com.bagro.entity.Empleado;
import com.bagro.entity.Pago;
import com.bagro.repository.EmpleadoRepository;
import com.bagro.repository.PagoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.bagro.entity.Asistencia;
import com.bagro.repository.AsistenciaRepository;
import com.bagro.entity.Solicitud;
import com.bagro.repository.SolicitudRepository;
import com.bagro.entity.Compra;
import com.bagro.entity.DetalleCompra;
import com.bagro.entity.EstadoCompra;
import com.bagro.repository.CompraRepository;

import java.time.Duration;
import java.time.LocalDate;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReporteExcelService {

    private final EmpleadoRepository empleadoRepository;
    private final PagoRepository pagoRepository;
    private final AuditoriaService auditoriaService;
    private final AsistenciaRepository asistenciaRepository;
    private final CompraRepository compraRepository;
    private final SolicitudRepository solicitudRepository;


    public ReporteExcelService(EmpleadoRepository empleadoRepository,
                               PagoRepository pagoRepository,
                               AsistenciaRepository asistenciaRepository,
                               SolicitudRepository solicitudRepository,
                               CompraRepository compraRepository,
                               AuditoriaService auditoriaService) {
        this.empleadoRepository = empleadoRepository;
        this.pagoRepository = pagoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.solicitudRepository = solicitudRepository;
        this.compraRepository = compraRepository;
        this.auditoriaService = auditoriaService;
    }

    public byte[] generarReporteTrabajadores() {
        List<Empleado> empleados = empleadoRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Trabajadores");

            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook);

            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE TRABAJADORES - BAGRO");
            tituloCell.setCellStyle(tituloStyle);

            Row header = sheet.createRow(2);

            String[] columnas = {
                    "ID",
                    "DNI",
                    "Nombres",
                    "Apellidos",
                    "Cargo",
                    "Área",
                    "Sueldo base",
                    "Estado",
                    "Usuario"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 3;

            for (Empleado e : empleados) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getDni());
                row.createCell(2).setCellValue(e.getNombres());
                row.createCell(3).setCellValue(e.getApellidos());
                row.createCell(4).setCellValue(e.getCargo());
                row.createCell(5).setCellValue(e.getArea());

                Cell sueldoCell = row.createCell(6);
                sueldoCell.setCellValue(e.getSueldoBase() != null ? e.getSueldoBase() : 0.0);
                sueldoCell.setCellStyle(moneyStyle);

                row.createCell(7).setCellValue(e.isActivo() ? "ACTIVO" : "INACTIVO");
                row.createCell(8).setCellValue(e.getUser() != null ? e.getUser().getUsername() : "-");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            auditoriaService.registrar(
                    "REPORTES",
                    "EXPORTAR TRABAJADORES EXCEL",
                    "Se exportó el reporte de trabajadores en formato Excel"
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel de trabajadores: " + e.getMessage());
        }
    }

    public byte[] generarReportePagos(Integer mes, Integer anio) {
        List<Pago> pagos = pagoRepository.findByMesAndAnio(mes, anio);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Pagos");

            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook);

            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE PAGOS Y PLANILLA - BAGRO");
            tituloCell.setCellStyle(tituloStyle);

            Row periodoRow = sheet.createRow(1);
            periodoRow.createCell(0).setCellValue("Periodo:");
            periodoRow.createCell(1).setCellValue("Mes " + mes + " - Año " + anio);

            Row header = sheet.createRow(3);

            String[] columnas = {
                    "ID Pago",
                    "Fecha registro",
                    "Mes",
                    "Año",
                    "Empleado",
                    "DNI",
                    "Sueldo base",
                    "Horas extra",
                    "Bonos",
                    "Descuentos",
                    "Total neto",
                    "Estado pago"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;
            double totalSueldoBase = 0.0;
            double totalHorasExtra = 0.0;
            double totalBonos = 0.0;
            double totalDescuentos = 0.0;
            double totalNeto = 0.0;

            for (Pago p : pagos) {
                Row row = sheet.createRow(rowNum++);

                String empleado = p.getEmpleado().getNombres() + " " + p.getEmpleado().getApellidos();

                double sueldoBase = p.getSueldoBase() != null ? p.getSueldoBase() : 0.0;
                double horasExtra = p.getHorasExtra() != null ? p.getHorasExtra() : 0.0;
                double bonos = p.getBonos() != null ? p.getBonos() : 0.0;
                double descuentos = p.getDescuentos() != null ? p.getDescuentos() : 0.0;
                double neto = p.getTotalNeto() != null ? p.getTotalNeto() : 0.0;

                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getFecha() != null ? p.getFecha().toString() : "-");
                row.createCell(2).setCellValue(p.getMes() != null ? p.getMes() : 0);
                row.createCell(3).setCellValue(p.getAnio() != null ? p.getAnio() : 0);
                row.createCell(4).setCellValue(empleado);
                row.createCell(5).setCellValue(p.getEmpleado().getDni());

                Cell sueldoCell = row.createCell(6);
                sueldoCell.setCellValue(sueldoBase);
                sueldoCell.setCellStyle(moneyStyle);

                Cell horasExtraCell = row.createCell(7);
                horasExtraCell.setCellValue(horasExtra);
                horasExtraCell.setCellStyle(moneyStyle);

                Cell bonosCell = row.createCell(8);
                bonosCell.setCellValue(bonos);
                bonosCell.setCellStyle(moneyStyle);

                Cell descuentosCell = row.createCell(9);
                descuentosCell.setCellValue(descuentos);
                descuentosCell.setCellStyle(moneyStyle);

                Cell totalNetoCell = row.createCell(10);
                totalNetoCell.setCellValue(neto);
                totalNetoCell.setCellStyle(moneyStyle);

                row.createCell(11).setCellValue(
                        p.getEstado() != null ? p.getEstado().name() : "PAGADO"
                );

                totalSueldoBase += sueldoBase;
                totalHorasExtra += horasExtra;
                totalBonos += bonos;
                totalDescuentos += descuentos;
                totalNeto += neto;
            }

            Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(5).setCellValue("TOTALES:");

            Cell totalSueldoCell = totalRow.createCell(6);
            totalSueldoCell.setCellValue(totalSueldoBase);
            totalSueldoCell.setCellStyle(moneyStyle);

            Cell totalHorasExtraCell = totalRow.createCell(7);
            totalHorasExtraCell.setCellValue(totalHorasExtra);
            totalHorasExtraCell.setCellStyle(moneyStyle);

            Cell totalBonosCell = totalRow.createCell(8);
            totalBonosCell.setCellValue(totalBonos);
            totalBonosCell.setCellStyle(moneyStyle);

            Cell totalDescuentosCell = totalRow.createCell(9);
            totalDescuentosCell.setCellValue(totalDescuentos);
            totalDescuentosCell.setCellStyle(moneyStyle);

            Cell totalNetoCell = totalRow.createCell(10);
            totalNetoCell.setCellValue(totalNeto);
            totalNetoCell.setCellStyle(moneyStyle);

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            auditoriaService.registrar(
                    "REPORTES",
                    "EXPORTAR PAGOS EXCEL",
                    "Se exportó el reporte de pagos en Excel para el mes " + mes + " y año " + anio
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel de pagos: " + e.getMessage());
        }
    }

    public byte[] generarReporteAsistencias(LocalDate desde, LocalDate hasta) {
        List<Asistencia> asistencias = asistenciaRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Asistencias");

            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle decimalStyle = crearEstiloDecimal(workbook);

            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE ASISTENCIAS - BAGRO");
            tituloCell.setCellStyle(tituloStyle);

            Row periodoRow = sheet.createRow(1);
            periodoRow.createCell(0).setCellValue("Desde:");
            periodoRow.createCell(1).setCellValue(desde.toString());
            periodoRow.createCell(2).setCellValue("Hasta:");
            periodoRow.createCell(3).setCellValue(hasta.toString());

            Row header = sheet.createRow(3);

            String[] columnas = {
                    "ID",
                    "Fecha",
                    "Empleado",
                    "DNI",
                    "Hora entrada",
                    "Hora salida",
                    "Estado",
                    "Horas trabajadas"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;
            double totalHorasTrabajadas = 0.0;
            int jornadasFinalizadas = 0;
            int jornadasIniciadas = 0;

            for (Asistencia a : asistencias) {
                Row row = sheet.createRow(rowNum++);

                String empleado = a.getEmpleado().getNombres() + " " + a.getEmpleado().getApellidos();

                double horasTrabajadas = 0.0;

                if (a.getHoraEntrada() != null && a.getHoraSalida() != null) {
                    long minutos = Duration.between(a.getHoraEntrada(), a.getHoraSalida()).toMinutes();
                    horasTrabajadas = minutos / 60.0;
                    totalHorasTrabajadas += horasTrabajadas;
                    jornadasFinalizadas++;
                } else {
                    jornadasIniciadas++;
                }

                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getFecha() != null ? a.getFecha().toString() : "-");
                row.createCell(2).setCellValue(empleado);
                row.createCell(3).setCellValue(a.getEmpleado().getDni());
                row.createCell(4).setCellValue(a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : "-");
                row.createCell(5).setCellValue(a.getHoraSalida() != null ? a.getHoraSalida().toString() : "-");
                row.createCell(6).setCellValue(a.getEstado() != null ? a.getEstado().name() : "-");

                Cell horasCell = row.createCell(7);
                horasCell.setCellValue(horasTrabajadas);
                horasCell.setCellStyle(decimalStyle);
            }

            Row resumenRow1 = sheet.createRow(rowNum + 1);
            resumenRow1.createCell(6).setCellValue("TOTAL HORAS:");
            Cell totalHorasCell = resumenRow1.createCell(7);
            totalHorasCell.setCellValue(totalHorasTrabajadas);
            totalHorasCell.setCellStyle(decimalStyle);

            Row resumenRow2 = sheet.createRow(rowNum + 2);
            resumenRow2.createCell(6).setCellValue("JORNADAS FINALIZADAS:");
            resumenRow2.createCell(7).setCellValue(jornadasFinalizadas);

            Row resumenRow3 = sheet.createRow(rowNum + 3);
            resumenRow3.createCell(6).setCellValue("JORNADAS EN CURSO:");
            resumenRow3.createCell(7).setCellValue(jornadasIniciadas);

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            auditoriaService.registrar(
                    "REPORTES",
                    "EXPORTAR ASISTENCIAS EXCEL",
                    "Se exportó el reporte de asistencias en Excel desde " + desde + " hasta " + hasta
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel de asistencias: " + e.getMessage());
        }
    }

    public byte[] generarReporteSolicitudes() {
        List<Solicitud> solicitudes = solicitudRepository.findAllByOrderByIdDesc();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Solicitudes");

            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);

            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE SOLICITUDES - BAGRO");
            tituloCell.setCellStyle(tituloStyle);

            Row header = sheet.createRow(2);

            String[] columnas = {
                    "ID",
                    "Tipo",
                    "Empleado",
                    "DNI",
                    "Fecha inicio",
                    "Fecha fin",
                    "Descripción",
                    "Estado",
                    "Comentario"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 3;

            int pendientes = 0;
            int aprobadas = 0;
            int rechazadas = 0;

            for (Solicitud s : solicitudes) {
                Row row = sheet.createRow(rowNum++);

                String empleado = s.getEmpleado().getNombres() + " " + s.getEmpleado().getApellidos();

                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getTipo());
                row.createCell(2).setCellValue(empleado);
                row.createCell(3).setCellValue(s.getEmpleado().getDni());
                row.createCell(4).setCellValue(s.getFechaInicio() != null ? s.getFechaInicio().toString() : "-");
                row.createCell(5).setCellValue(s.getFechaFin() != null ? s.getFechaFin().toString() : "-");
                row.createCell(6).setCellValue(s.getDescripcion());
                row.createCell(7).setCellValue(s.getEstado() != null ? s.getEstado().name() : "-");
                row.createCell(8).setCellValue(s.getComentario() != null ? s.getComentario() : "-");

                if (s.getEstado() != null) {
                    switch (s.getEstado()) {
                        case PENDIENTE -> pendientes++;
                        case APROBADO -> aprobadas++;
                        case RECHAZADO -> rechazadas++;
                    }
                }
            }

            Row resumenRow1 = sheet.createRow(rowNum + 1);
            resumenRow1.createCell(7).setCellValue("TOTAL SOLICITUDES:");
            resumenRow1.createCell(8).setCellValue(solicitudes.size());

            Row resumenRow2 = sheet.createRow(rowNum + 2);
            resumenRow2.createCell(7).setCellValue("PENDIENTES:");
            resumenRow2.createCell(8).setCellValue(pendientes);

            Row resumenRow3 = sheet.createRow(rowNum + 3);
            resumenRow3.createCell(7).setCellValue("APROBADAS:");
            resumenRow3.createCell(8).setCellValue(aprobadas);

            Row resumenRow4 = sheet.createRow(rowNum + 4);
            resumenRow4.createCell(7).setCellValue("RECHAZADAS:");
            resumenRow4.createCell(8).setCellValue(rechazadas);

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            auditoriaService.registrar(
                    "REPORTES",
                    "EXPORTAR SOLICITUDES EXCEL",
                    "Se exportó el reporte de solicitudes en formato Excel"
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel de solicitudes: " + e.getMessage());
        }
    }

    public byte[] generarReporteCompras(LocalDate desde, LocalDate hasta) {
        List<Compra> compras = compraRepository.findByFechaBetween(desde, hasta);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Compras");

            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook);

            Row tituloRow = sheet.createRow(0);
            Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue("REPORTE DE COMPRAS - BAGRO");
            tituloCell.setCellStyle(tituloStyle);

            Row periodoRow = sheet.createRow(1);
            periodoRow.createCell(0).setCellValue("Desde:");
            periodoRow.createCell(1).setCellValue(desde.toString());
            periodoRow.createCell(2).setCellValue("Hasta:");
            periodoRow.createCell(3).setCellValue(hasta.toString());

            Row header = sheet.createRow(3);

            String[] columnas = {
                    "ID Compra",
                    "Fecha",
                    "Tipo comprobante",
                    "N° comprobante",
                    "Proveedor",
                    "Producto",
                    "Cantidad",
                    "Precio unitario",
                    "Subtotal producto",
                    "Subtotal compra",
                    "IGV",
                    "Total compra",
                    "Estado",
                    "Observación"
            };

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;

            double subtotalGeneral = 0.0;
            double igvGeneral = 0.0;
            double totalGeneral = 0.0;

            int comprasRegistradas = 0;
            int comprasAnuladas = 0;

            for (Compra compra : compras) {

                boolean compraRegistrada = compra.getEstado() == null || compra.getEstado() == EstadoCompra.REGISTRADA;
                boolean compraAnulada = compra.getEstado() == EstadoCompra.ANULADA;

                if (compraRegistrada) {
                    comprasRegistradas++;
                    subtotalGeneral += compra.getSubtotal() != null ? compra.getSubtotal() : 0.0;
                    igvGeneral += compra.getIgv() != null ? compra.getIgv() : 0.0;
                    totalGeneral += compra.getTotal() != null ? compra.getTotal() : 0.0;
                }

                if (compraAnulada) {
                    comprasAnuladas++;
                }

                String proveedor = compra.getProveedor() != null
                        ? compra.getProveedor().getRazonSocial()
                        : "-";

                String estado = compra.getEstado() != null
                        ? compra.getEstado().name()
                        : "REGISTRADA";

                double subtotalCompraMostrar = compra.getSubtotal() != null ? compra.getSubtotal()
                        : (compra.getTotal() != null ? compra.getTotal() : 0.0);

                double igvCompraMostrar = compra.getIgv() != null ? compra.getIgv() : 0.0;

                double totalCompraMostrar = compra.getTotal() != null ? compra.getTotal() : 0.0;

                if (compra.getDetalles() != null && !compra.getDetalles().isEmpty()) {

                    boolean primeraFilaCompra = true;

                    for (DetalleCompra detalle : compra.getDetalles()) {
                        Row row = sheet.createRow(rowNum++);

                        Cell idCell = row.createCell(0);
                        if (primeraFilaCompra) {
                            idCell.setCellValue(compra.getId());
                        } else {
                            idCell.setCellValue("");
                        }

                        row.createCell(1).setCellValue(
                                primeraFilaCompra && compra.getFecha() != null ? compra.getFecha().toString() : ""
                        );

                        row.createCell(2).setCellValue(
                                primeraFilaCompra && compra.getTipoComprobante() != null ? compra.getTipoComprobante() : ""
                        );

                        row.createCell(3).setCellValue(
                                primeraFilaCompra && compra.getNumeroComprobante() != null ? compra.getNumeroComprobante() : ""
                        );

                        row.createCell(4).setCellValue(
                                primeraFilaCompra ? proveedor : ""
                        );

                        row.createCell(5).setCellValue(detalle.getNombreProducto());
                        row.createCell(6).setCellValue(detalle.getCantidad() != null ? detalle.getCantidad() : 0);

                        Cell precioCell = row.createCell(7);
                        precioCell.setCellValue(detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0);
                        precioCell.setCellStyle(moneyStyle);

                        Cell subtotalProductoCell = row.createCell(8);
                        subtotalProductoCell.setCellValue(detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0);
                        subtotalProductoCell.setCellStyle(moneyStyle);

                        Cell subtotalCompraCell = row.createCell(9);
                        if (primeraFilaCompra) {
                            subtotalCompraCell.setCellValue(subtotalCompraMostrar);
                            subtotalCompraCell.setCellStyle(moneyStyle);
                        } else {
                            subtotalCompraCell.setCellValue("");
                        }

                        Cell igvCell = row.createCell(10);
                        if (primeraFilaCompra) {
                            igvCell.setCellValue(igvCompraMostrar);
                            igvCell.setCellStyle(moneyStyle);
                        } else {
                            igvCell.setCellValue("");
                        }

                        Cell totalCompraCell = row.createCell(11);
                        if (primeraFilaCompra) {
                            totalCompraCell.setCellValue(totalCompraMostrar);
                            totalCompraCell.setCellStyle(moneyStyle);
                        } else {
                            totalCompraCell.setCellValue("");
                        }

                        row.createCell(12).setCellValue(primeraFilaCompra ? estado : "");
                        row.createCell(13).setCellValue(primeraFilaCompra && compra.getObservacion() != null ? compra.getObservacion() : "");

                        primeraFilaCompra = false;
                    }

                } else {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(compra.getId());
                    row.createCell(1).setCellValue(compra.getFecha() != null ? compra.getFecha().toString() : "-");
                    row.createCell(2).setCellValue(compra.getTipoComprobante() != null ? compra.getTipoComprobante() : "-");
                    row.createCell(3).setCellValue(compra.getNumeroComprobante() != null ? compra.getNumeroComprobante() : "-");
                    row.createCell(4).setCellValue(proveedor);
                    row.createCell(5).setCellValue("-");
                    row.createCell(6).setCellValue(0);

                    Cell precioCell = row.createCell(7);
                    precioCell.setCellValue(0.0);
                    precioCell.setCellStyle(moneyStyle);

                    Cell subtotalProductoCell = row.createCell(8);
                    subtotalProductoCell.setCellValue(0.0);
                    subtotalProductoCell.setCellStyle(moneyStyle);

                    Cell subtotalCompraCell = row.createCell(9);
                    subtotalCompraCell.setCellValue(subtotalCompraMostrar);
                    subtotalCompraCell.setCellStyle(moneyStyle);

                    Cell igvCell = row.createCell(10);
                    igvCell.setCellValue(igvCompraMostrar);
                    igvCell.setCellStyle(moneyStyle);

                    Cell totalCompraCell = row.createCell(11);
                    totalCompraCell.setCellValue(totalCompraMostrar);
                    totalCompraCell.setCellStyle(moneyStyle);

                    row.createCell(12).setCellValue(estado);
                    row.createCell(13).setCellValue(compra.getObservacion() != null ? compra.getObservacion() : "-");
                }
            }

            Row resumenRow1 = sheet.createRow(rowNum + 1);
            resumenRow1.createCell(10).setCellValue("SUBTOTAL GENERAL:");
            Cell subtotalGeneralCell = resumenRow1.createCell(11);
            subtotalGeneralCell.setCellValue(subtotalGeneral);
            subtotalGeneralCell.setCellStyle(moneyStyle);

            Row resumenRow2 = sheet.createRow(rowNum + 2);
            resumenRow2.createCell(10).setCellValue("IGV GENERAL:");
            Cell igvGeneralCell = resumenRow2.createCell(11);
            igvGeneralCell.setCellValue(igvGeneral);
            igvGeneralCell.setCellStyle(moneyStyle);

            Row resumenRow3 = sheet.createRow(rowNum + 3);
            resumenRow3.createCell(10).setCellValue("TOTAL GENERAL:");
            Cell totalGeneralCell = resumenRow3.createCell(11);
            totalGeneralCell.setCellValue(totalGeneral);
            totalGeneralCell.setCellStyle(moneyStyle);

            Row resumenRow4 = sheet.createRow(rowNum + 4);
            resumenRow4.createCell(10).setCellValue("COMPRAS REGISTRADAS:");
            resumenRow4.createCell(11).setCellValue(comprasRegistradas);

            Row resumenRow5 = sheet.createRow(rowNum + 5);
            resumenRow5.createCell(10).setCellValue("COMPRAS ANULADAS:");
            resumenRow5.createCell(11).setCellValue(comprasAnuladas);

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            auditoriaService.registrar(
                    "REPORTES",
                    "EXPORTAR COMPRAS EXCEL",
                    "Se exportó el reporte de compras en Excel desde " + desde + " hasta " + hasta
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel de compras: " + e.getMessage());
        }
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        style.setFont(font);

        return style;
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("\"S/\" #,##0.00"));

        return style;
    }

    private CellStyle crearEstiloDecimal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));

        return style;
    }
}