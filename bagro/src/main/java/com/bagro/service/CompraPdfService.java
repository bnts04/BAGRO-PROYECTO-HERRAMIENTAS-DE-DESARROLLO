package com.bagro.service;

import com.bagro.entity.Compra;
import com.bagro.entity.DetalleCompra;
import com.bagro.entity.EstadoCompra;
import com.bagro.repository.CompraRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class CompraPdfService {

    private static final String EMPRESA_NOMBRE = "BAGRO S.A.C.";
    private static final String EMPRESA_RUC = "20501234567";
    private static final String EMPRESA_SISTEMA = "Sistema Agroindustrial BAGRO";
    private static final String AREA_EMISORA = "Compras / Administración";

    private final CompraRepository compraRepository;

    public CompraPdfService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    public byte[] generarComprobanteCompra(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 45, 45, 30, 30);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 90, 30));
            Font subtituloFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
            Font textoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 90, 30));
            Font alertaFont = new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE);

            agregarEncabezado(document, compra, tituloFont, textoFont);
            agregarSeparador(document);

            if (compra.getEstado() == EstadoCompra.ANULADA) {
                agregarAvisoAnulado(document, alertaFont);
            }

            agregarDatosEmpresa(document, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDatosProveedor(document, compra, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDatosCompra(document, compra, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDetalleProductos(document, compra, subtituloFont, headerFont, textoFont, totalFont);

            document.add(new Paragraph(" "));

            agregarPie(document, textoFont);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF: " + e.getMessage());
        }
    }

    public String generarNombreArchivoComprobante(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        String tipo = compra.getTipoComprobante() != null ? compra.getTipoComprobante() : "COMPROBANTE";
        String numero = compra.getNumeroComprobante() != null ? compra.getNumeroComprobante() : "SIN-NUMERO";

        String nombre = "comprobante-compra-" + tipo + "-" + numero + "-BAGRO.pdf";

        return limpiarNombreArchivo(nombre);
    }

    private String limpiarNombreArchivo(String nombre) {
        return nombre
                .replace(" ", "-")
                .replace("/", "-")
                .replace("\\", "-")
                .replace(":", "-")
                .replace("*", "-")
                .replace("?", "-")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "");
    }

    private void agregarEncabezado(Document document, Compra compra, Font tituloFont, Font textoFont) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{42, 58});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        try {
            ClassPathResource logoResource = new ClassPathResource("static/img/bagro-logo.png");

            if (logoResource.exists()) {
                Image logo = Image.getInstance(IOUtils.toByteArray(logoResource.getInputStream()));
                logo.scaleToFit(170, 90);
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

        Paragraph documento = new Paragraph("COMPROBANTE INTERNO DE COMPRA", new Font(Font.HELVETICA, 15, Font.BOLD));
        documento.setAlignment(Element.ALIGN_RIGHT);
        documento.setSpacingBefore(8);

        Paragraph comprobante = new Paragraph(
                obtenerTextoComprobante(compra),
                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 90, 30))
        );
        comprobante.setAlignment(Element.ALIGN_RIGHT);
        comprobante.setSpacingBefore(5);

        infoCell.addElement(empresa);
        infoCell.addElement(subtitulo);
        infoCell.addElement(documento);
        infoCell.addElement(comprobante);

        header.addCell(logoCell);
        header.addCell(infoCell);

        document.add(header);
    }

    private String obtenerTextoComprobante(Compra compra) {
        String tipo = compra.getTipoComprobante() != null ? compra.getTipoComprobante() : "-";
        String numero = compra.getNumeroComprobante() != null ? compra.getNumeroComprobante() : "-";
        return tipo + " N.° " + numero;
    }

    private void agregarSeparador(Document document) throws Exception {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(8);
        linea.setSpacingAfter(12);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidthBottom(1.5f);
        cell.setBorderColor(new Color(0, 100, 40));
        cell.setFixedHeight(5f);

        linea.addCell(cell);
        document.add(linea);
    }

    private void agregarAvisoAnulado(Document document, Font alertaFont) throws Exception {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(12);

        PdfPCell cell = new PdfPCell(new Phrase("DOCUMENTO ANULADO", alertaFont));
        cell.setBackgroundColor(new Color(180, 0, 0));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);

        tabla.addCell(cell);
        document.add(tabla);
    }

    private void agregarDatosEmpresa(Document document, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DE LA EMPRESA", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "Empresa", EMPRESA_NOMBRE, textoFont);
        agregarFilaSimple(tabla, "RUC", EMPRESA_RUC, textoFont);
        agregarFilaSimple(tabla, "Sistema", EMPRESA_SISTEMA, textoFont);
        agregarFilaSimple(tabla, "Área emisora", AREA_EMISORA, textoFont);
        agregarFilaSimple(tabla, "Fecha de emisión", String.valueOf(LocalDate.now()), textoFont);

        document.add(tabla);
    }

    private void agregarDatosProveedor(Document document, Compra compra, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DEL PROVEEDOR", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "RUC", compra.getProveedor() != null ? compra.getProveedor().getRuc() : "-", textoFont);
        agregarFilaSimple(tabla, "Razón social", compra.getProveedor() != null ? compra.getProveedor().getRazonSocial() : "-", textoFont);
        agregarFilaSimple(tabla, "Tipo de producto", compra.getProveedor() != null ? compra.getProveedor().getTipoProducto() : "-", textoFont);
        agregarFilaSimple(tabla, "Estado proveedor", compra.getProveedor() != null && compra.getProveedor().getEstado() != null ? compra.getProveedor().getEstado().name() : "-", textoFont);
        agregarFilaSimple(tabla, "Observación proveedor", compra.getProveedor() != null ? compra.getProveedor().getObservacion() : "-", textoFont);

        document.add(tabla);
    }

    private void agregarDatosCompra(Document document, Compra compra, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DE LA COMPRA", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "Código de compra", String.valueOf(compra.getId()), textoFont);
        agregarFilaSimple(tabla, "Fecha de compra", compra.getFecha() != null ? compra.getFecha().toString() : "-", textoFont);
        agregarFilaSimple(tabla, "Tipo de comprobante", compra.getTipoComprobante(), textoFont);
        agregarFilaSimple(tabla, "Número de comprobante", compra.getNumeroComprobante(), textoFont);
        agregarFilaSimple(tabla, "Estado de compra", compra.getEstado() != null ? compra.getEstado().name() : "REGISTRADA", textoFont);
        agregarFilaSimple(tabla, "Cantidad de productos", compra.getDetalles() != null ? String.valueOf(compra.getDetalles().size()) : "0", textoFont);
        agregarFilaSimple(tabla, "Observación de compra", compra.getObservacion(), textoFont);

        document.add(tabla);
    }

    private void agregarDetalleProductos(Document document,
                                         Compra compra,
                                         Font subtituloFont,
                                         Font headerFont,
                                         Font textoFont,
                                         Font totalFont) throws Exception {
        document.add(new Paragraph("DETALLE DE PRODUCTOS", subtituloFont));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[]{45, 15, 20, 20});

        agregarHeader(tabla, "Producto", headerFont);
        agregarHeader(tabla, "Cantidad", headerFont);
        agregarHeader(tabla, "Precio unit.", headerFont);
        agregarHeader(tabla, "Subtotal", headerFont);

        if (compra.getDetalles() != null) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                agregarCelda(tabla, detalle.getNombreProducto(), textoFont, Element.ALIGN_LEFT);
                agregarCelda(tabla, String.valueOf(detalle.getCantidad()), textoFont, Element.ALIGN_CENTER);
                agregarCelda(tabla, "S/ " + formatoMonto(detalle.getPrecioUnitario()), textoFont, Element.ALIGN_RIGHT);
                agregarCelda(tabla, "S/ " + formatoMonto(detalle.getSubtotal()), textoFont, Element.ALIGN_RIGHT);
            }
        }

        agregarFilaResumen(tabla, "SUBTOTAL", compra.getSubtotal(), totalFont);
        agregarFilaResumen(tabla, "IGV (18%)", compra.getIgv(), totalFont);
        agregarFilaResumenFinal(tabla, "TOTAL COMPRA", compra.getTotal(), totalFont);

        tabla.setKeepTogether(true);
        document.add(tabla);
    }

    private void agregarFilaResumen(PdfPTable tabla, String label, Double monto, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setColspan(3);
        labelCell.setPadding(8);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        tabla.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase("S/ " + formatoMonto(monto), font));
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new Color(245, 245, 245));
        tabla.addCell(valueCell);
    }

    private void agregarFilaResumenFinal(PdfPTable tabla, String label, Double monto, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setColspan(3);
        labelCell.setPadding(9);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase("S/ " + formatoMonto(monto), font));
        valueCell.setPadding(9);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(valueCell);
    }

    private void agregarPie(Document document, Font textoFont) throws Exception {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setWidths(new float[]{50, 50});
        firmas.setSpacingBefore(15);

        PdfPCell compras = new PdfPCell(new Phrase("____________________________\nÁrea de Compras", textoFont));
        compras.setBorder(Rectangle.NO_BORDER);
        compras.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell administracion = new PdfPCell(new Phrase("____________________________\nAdministración", textoFont));
        administracion.setBorder(Rectangle.NO_BORDER);
        administracion.setHorizontalAlignment(Element.ALIGN_CENTER);

        firmas.addCell(compras);
        firmas.addCell(administracion);

        document.add(firmas);

        Paragraph nota = new Paragraph("\nDocumento generado automáticamente por el Sistema Agroindustrial BAGRO.", textoFont);
        nota.setAlignment(Element.ALIGN_CENTER);
        document.add(nota);
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

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null && !valor.isBlank() ? valor : "-", font));
        c2.setPadding(6);
        tabla.addCell(c2);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        cell.setPadding(7);
        cell.setHorizontalAlignment(align);
        tabla.addCell(cell);
    }

    private String formatoMonto(Double monto) {
        return String.format(Locale.US, "%.2f", monto != null ? monto : 0.0);
    }
}