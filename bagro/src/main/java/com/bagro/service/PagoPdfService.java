package com.bagro.service;

import com.bagro.entity.Asistencia;
import com.bagro.entity.Pago;
import com.bagro.repository.AsistenciaRepository;
import com.bagro.repository.PagoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.apache.commons.io.IOUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PagoPdfService {

    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;

    public PagoPdfService(PagoRepository pagoRepository,
                          AsistenciaRepository asistenciaRepository) {
        this.pagoRepository = pagoRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    public byte[] generarBoletaPago(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 45, 45, 35, 35);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 90, 30));
            Font subtituloFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
            Font textoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font textoBoldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 90, 30));

            agregarEncabezado(document, tituloFont, textoFont);
            agregarSeparador(document);

            agregarDatosTrabajador(document, pago, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDatosPeriodo(document, pago, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarResumenAsistencia(document, pago, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDetallePago(document, pago, subtituloFont, headerFont, textoFont, totalFont);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            agregarFirmasYPie(document, textoFont, textoBoldFont);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar la boleta PDF: " + e.getMessage());
        }
    }

    private void agregarEncabezado(Document document, Font tituloFont, Font textoFont) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{45, 55});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try {
            ClassPathResource logoResource = new ClassPathResource("static/img/bagro-logo.png");

            if (logoResource.exists()) {
                Image logo = Image.getInstance(IOUtils.toByteArray(logoResource.getInputStream()));
                logo.scaleToFit(210, 120);
                logo.setAlignment(Image.ALIGN_LEFT);
                logoCell.addElement(logo);
            } else {
                logoCell.addElement(new Paragraph("BAGRO", tituloFont));
            }

        } catch (Exception e) {
            logoCell.addElement(new Paragraph("BAGRO", tituloFont));
        }

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph empresa = new Paragraph("BAGRO", tituloFont);
        empresa.setAlignment(Element.ALIGN_RIGHT);

        Paragraph subtitulo = new Paragraph("Sistema Agroindustrial", textoFont);
        subtitulo.setAlignment(Element.ALIGN_RIGHT);

        Paragraph documento = new Paragraph("BOLETA DE PAGO", new Font(Font.HELVETICA, 16, Font.BOLD));
        documento.setAlignment(Element.ALIGN_RIGHT);
        documento.setSpacingBefore(8);

        infoCell.addElement(empresa);
        infoCell.addElement(subtitulo);
        infoCell.addElement(documento);

        header.addCell(logoCell);
        header.addCell(infoCell);

        document.add(header);
    }

    private void agregarSeparador(Document document) throws Exception {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(10);
        linea.setSpacingAfter(15);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidthBottom(1.5f);
        cell.setBorderColor(new Color(0, 100, 40));
        cell.setFixedHeight(5f);

        linea.addCell(cell);
        document.add(linea);
    }

    private void agregarDatosTrabajador(Document document, Pago pago, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DEL TRABAJADOR", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "Nombre completo", pago.getEmpleado().getNombres() + " " + pago.getEmpleado().getApellidos(), textoFont);
        agregarFilaSimple(tabla, "DNI", pago.getEmpleado().getDni(), textoFont);
        agregarFilaSimple(tabla, "Cargo", pago.getEmpleado().getCargo(), textoFont);
        agregarFilaSimple(tabla, "Área", pago.getEmpleado().getArea(), textoFont);
        agregarFilaSimple(tabla, "Estado", pago.getEmpleado().isActivo() ? "Activo" : "Inactivo", textoFont);

        document.add(tabla);
    }

    private void agregarDatosPeriodo(Document document, Pago pago, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DEL PERIODO", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "Periodo", nombreMes(pago.getMes()) + " " + pago.getAnio(), textoFont);
        agregarFilaSimple(tabla, "Fecha de registro", String.valueOf(pago.getFecha()), textoFont);
        agregarFilaSimple(tabla, "Fecha de emisión", String.valueOf(LocalDate.now()), textoFont);

        document.add(tabla);
    }

    private void agregarResumenAsistencia(Document document, Pago pago, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("RESUMEN DE ASISTENCIA", subtituloFont));

        YearMonth periodo = YearMonth.of(pago.getAnio(), pago.getMes());
        LocalDate desde = periodo.atDay(1);
        LocalDate hasta = periodo.atEndOfMonth();

        List<Asistencia> asistencias = asistenciaRepository.findByEmpleadoAndFechaBetween(
                pago.getEmpleado(),
                desde,
                hasta
        );

        int diasRegistrados = asistencias.size();

        int jornadasCompletas = (int) asistencias.stream()
                .filter(a -> a.getHoraEntrada() != null && a.getHoraSalida() != null)
                .count();

        int jornadasIncompletas = diasRegistrados - jornadasCompletas;

        double horasTrabajadas = asistencias.stream()
                .filter(a -> a.getHoraEntrada() != null && a.getHoraSalida() != null)
                .mapToDouble(a -> Duration.between(a.getHoraEntrada(), a.getHoraSalida()).toMinutes() / 60.0)
                .sum();

        double horasExtraEstimadas = asistencias.stream()
                .filter(a -> a.getHoraEntrada() != null && a.getHoraSalida() != null)
                .mapToDouble(a -> {
                    double horasDia = Duration.between(a.getHoraEntrada(), a.getHoraSalida()).toMinutes() / 60.0;
                    return Math.max(horasDia - 8.0, 0.0);
                })
                .sum();

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{40, 60});

        agregarFilaSimple(tabla, "Días con asistencia registrada", String.valueOf(diasRegistrados), textoFont);
        agregarFilaSimple(tabla, "Jornadas completas", String.valueOf(jornadasCompletas), textoFont);
        agregarFilaSimple(tabla, "Jornadas incompletas", String.valueOf(jornadasIncompletas), textoFont);
        agregarFilaSimple(tabla, "Horas trabajadas registradas", formatoHoras(horasTrabajadas), textoFont);
        agregarFilaSimple(tabla, "Horas extra estimadas", formatoHoras(horasExtraEstimadas), textoFont);

        document.add(tabla);
    }

    private void agregarDetallePago(Document document, Pago pago, Font subtituloFont, Font headerFont, Font textoFont, Font totalFont) throws Exception {
        document.add(new Paragraph("DETALLE ECONÓMICO DEL PAGO", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[]{65, 35});

        agregarHeader(tabla, "Concepto", headerFont);
        agregarHeader(tabla, "Monto", headerFont);

        agregarFilaMonto(tabla, "Sueldo base", pago.getSueldoBase(), textoFont);
        agregarFilaMonto(tabla, "Monto por horas extra", pago.getHorasExtra(), textoFont);
        agregarFilaMonto(tabla, "Bonos", pago.getBonos(), textoFont);
        agregarFilaMonto(tabla, "Descuentos", pago.getDescuentos(), textoFont);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL NETO", totalFont));
        totalLabel.setPadding(8);
        totalLabel.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase("S/ " + formatoMonto(pago.getTotalNeto()), totalFont));
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValue.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(totalValue);

        document.add(tabla);
    }

    private void agregarFirmasYPie(Document document, Font textoFont, Font textoBoldFont) throws Exception {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setWidths(new float[]{50, 50});

        PdfPCell trabajador = new PdfPCell(new Phrase("____________________________\nFirma del trabajador", textoFont));
        trabajador.setBorder(Rectangle.NO_BORDER);
        trabajador.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell rrhh = new PdfPCell(new Phrase("____________________________\nÁrea de RR.HH.", textoFont));
        rrhh.setBorder(Rectangle.NO_BORDER);
        rrhh.setHorizontalAlignment(Element.ALIGN_CENTER);

        firmas.addCell(trabajador);
        firmas.addCell(rrhh);

        document.add(firmas);

        Paragraph nota = new Paragraph("\nDocumento generado automáticamente por el Sistema Agroindustrial BAGRO.", textoFont);
        nota.setAlignment(Element.ALIGN_CENTER);
        document.add(nota);

        Paragraph aviso = new Paragraph("La información de asistencia se calcula según los registros existentes en el sistema.", textoBoldFont);
        aviso.setAlignment(Element.ALIGN_CENTER);
        document.add(aviso);
    }

    private void agregarHeader(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new Color(0, 100, 40));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    private void agregarFilaSimple(PdfPTable tabla, String campo, String valor, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(campo, new Font(Font.HELVETICA, 10, Font.BOLD)));
        c1.setPadding(6);
        c1.setBackgroundColor(new Color(245, 245, 245));
        tabla.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", font));
        c2.setPadding(6);
        tabla.addCell(c2);
    }

    private void agregarFilaMonto(PdfPTable tabla, String concepto, Double monto, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(concepto, font));
        c1.setPadding(7);
        tabla.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase("S/ " + formatoMonto(monto), font));
        c2.setPadding(7);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(c2);
    }

    private String formatoMonto(Double monto) {
        return String.format("%.2f", monto != null ? monto : 0.0);
    }

    private String formatoHoras(Double horas) {
        return String.format("%.2f horas", horas != null ? horas : 0.0);
    }

    private String nombreMes(Integer mes) {
        if (mes == null) return "-";

        return switch (mes) {
            case 1 -> "Enero";
            case 2 -> "Febrero";
            case 3 -> "Marzo";
            case 4 -> "Abril";
            case 5 -> "Mayo";
            case 6 -> "Junio";
            case 7 -> "Julio";
            case 8 -> "Agosto";
            case 9 -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "Mes inválido";
        };
    }
}