package com.bagro.service;

import com.bagro.entity.Compra;
import com.bagro.entity.DetalleCompra;
import com.bagro.repository.CompraRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class CompraPdfService {

    private final CompraRepository compraRepository;

    public CompraPdfService(CompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    public byte[] generarComprobanteCompra(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 45, 45, 35, 35);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 90, 30));
            Font subtituloFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
            Font textoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 90, 30));

            agregarEncabezado(document, tituloFont, textoFont);
            agregarSeparador(document);

            agregarDatosProveedor(document, compra, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDatosCompra(document, compra, subtituloFont, textoFont);

            document.add(new Paragraph(" "));

            agregarDetalleProductos(document, compra, subtituloFont, headerFont, textoFont, totalFont);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            agregarPie(document, textoFont);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF: " + e.getMessage());
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
                Image logo = Image.getInstance(logoResource.getInputStream().readAllBytes());
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

        Paragraph documento = new Paragraph("COMPROBANTE DE COMPRA", new Font(Font.HELVETICA, 16, Font.BOLD));
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

    private void agregarDatosProveedor(Document document, Compra compra, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DEL PROVEEDOR", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "RUC", compra.getProveedor().getRuc(), textoFont);
        agregarFilaSimple(tabla, "Razón social", compra.getProveedor().getRazonSocial(), textoFont);
        agregarFilaSimple(tabla, "Tipo de producto", compra.getProveedor().getTipoProducto(), textoFont);
        agregarFilaSimple(tabla, "Estado", compra.getProveedor().getEstado().name(), textoFont);
        agregarFilaSimple(tabla, "Observación", compra.getProveedor().getObservacion(), textoFont);

        document.add(tabla);
    }

    private void agregarDatosCompra(Document document, Compra compra, Font subtituloFont, Font textoFont) throws Exception {
        document.add(new Paragraph("DATOS DE LA COMPRA", subtituloFont));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8);
        tabla.setWidths(new float[]{30, 70});

        agregarFilaSimple(tabla, "Código de compra", String.valueOf(compra.getId()), textoFont);
        agregarFilaSimple(tabla, "Fecha de compra", String.valueOf(compra.getFecha()), textoFont);
        agregarFilaSimple(tabla, "Fecha de emisión", String.valueOf(LocalDate.now()), textoFont);
        agregarFilaSimple(tabla, "Cantidad de productos", String.valueOf(compra.getDetalles().size()), textoFont);

        document.add(tabla);
    }

    private void agregarDetalleProductos(Document document, Compra compra, Font subtituloFont, Font headerFont, Font textoFont, Font totalFont) throws Exception {
        document.add(new Paragraph("DETALLE DE PRODUCTOS", subtituloFont));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        tabla.setWidths(new float[]{45, 15, 20, 20});

        agregarHeader(tabla, "Producto", headerFont);
        agregarHeader(tabla, "Cantidad", headerFont);
        agregarHeader(tabla, "Precio unit.", headerFont);
        agregarHeader(tabla, "Subtotal", headerFont);

        for (DetalleCompra detalle : compra.getDetalles()) {
            agregarCelda(tabla, detalle.getNombreProducto(), textoFont, Element.ALIGN_LEFT);
            agregarCelda(tabla, String.valueOf(detalle.getCantidad()), textoFont, Element.ALIGN_CENTER);
            agregarCelda(tabla, "S/ " + formatoMonto(detalle.getPrecioUnitario()), textoFont, Element.ALIGN_RIGHT);
            agregarCelda(tabla, "S/ " + formatoMonto(detalle.getSubtotal()), textoFont, Element.ALIGN_RIGHT);
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL COMPRA", totalFont));
        totalLabel.setColspan(3);
        totalLabel.setPadding(8);
        totalLabel.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase("S/ " + formatoMonto(compra.getTotal()), totalFont));
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValue.setBackgroundColor(new Color(230, 245, 230));
        tabla.addCell(totalValue);

        document.add(tabla);
    }

    private void agregarPie(Document document, Font textoFont) throws Exception {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setWidths(new float[]{50, 50});

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

        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "-", font));
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
        return String.format("%.2f", monto != null ? monto : 0.0);
    }
}