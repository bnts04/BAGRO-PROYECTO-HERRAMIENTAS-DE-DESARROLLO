package com.bagro.service;

import com.bagro.dto.request.CompraRequest;
import com.bagro.dto.request.DetalleCompraRequest;
import com.bagro.dto.response.CompraKpiResponse;
import com.bagro.dto.response.CompraResponse;
import com.bagro.dto.response.DetalleCompraResponse;
import com.bagro.entity.Compra;
import com.bagro.entity.DetalleCompra;
import com.bagro.entity.EstadoCompra;
import com.bagro.entity.Proveedor;
import com.bagro.repository.CompraRepository;
import com.bagro.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompraService {

    private static final double IGV_PORCENTAJE = 0.18;

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final AuditoriaService auditoriaService;

    public CompraService(CompraRepository compraRepository,
                         ProveedorRepository proveedorRepository,
                         AuditoriaService auditoriaService) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    public String crearCompra(CompraRequest request) {

        if (request.getProveedorId() == null) {
            throw new RuntimeException("Debe seleccionar un proveedor");
        }

        if (request.getProductos() == null || request.getProductos().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto a la compra");
        }

        if (request.getTipoComprobante() == null || request.getTipoComprobante().isBlank()) {
            throw new RuntimeException("Debe ingresar el tipo de comprobante");
        }

        if (request.getNumeroComprobante() == null || request.getNumeroComprobante().isBlank()) {
            throw new RuntimeException("Debe ingresar el número de comprobante");
        }

        String tipoComprobante = request.getTipoComprobante().trim().toUpperCase();
        String numeroComprobante = request.getNumeroComprobante().trim();

        if (compraRepository.existsByTipoComprobanteAndNumeroComprobante(tipoComprobante, numeroComprobante)) {
            throw new RuntimeException("Ya existe una compra registrada con ese tipo y número de comprobante");
        }

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        LocalDate fechaCompra = request.getFechaCompra() != null && !request.getFechaCompra().isBlank()
                ? LocalDate.parse(request.getFechaCompra())
                : LocalDate.now();

        Compra compra = Compra.builder()
                .fecha(fechaCompra)
                .tipoComprobante(tipoComprobante)
                .numeroComprobante(numeroComprobante)
                .observacion(request.getObservacion())
                .proveedor(proveedor)
                .subtotal(0.0)
                .igv(0.0)
                .total(0.0)
                .estado(EstadoCompra.REGISTRADA)
                .build();

        List<DetalleCompra> detalles = new ArrayList<>();

        double subtotalCompra = 0.0;

        for (DetalleCompraRequest item : request.getProductos()) {

            if (item.getNombreProducto() == null || item.getNombreProducto().isBlank()) {
                throw new RuntimeException("El nombre del producto es obligatorio");
            }

            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad del producto debe ser mayor a 0");
            }

            if (item.getPrecioUnitario() == null || item.getPrecioUnitario() <= 0) {
                throw new RuntimeException("El precio unitario debe ser mayor a 0");
            }

            double subtotalProducto = item.getCantidad() * item.getPrecioUnitario();

            DetalleCompra detalle = DetalleCompra.builder()
                    .nombreProducto(item.getNombreProducto())
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .subtotal(redondear(subtotalProducto))
                    .compra(compra)
                    .build();

            detalles.add(detalle);
            subtotalCompra += subtotalProducto;
        }

        double igv = subtotalCompra * IGV_PORCENTAJE;
        double total = subtotalCompra + igv;

        compra.setDetalles(detalles);
        compra.setSubtotal(redondear(subtotalCompra));
        compra.setIgv(redondear(igv));
        compra.setTotal(redondear(total));

        compraRepository.save(compra);

        auditoriaService.registrar(
                "COMPRAS",
                "REGISTRAR COMPRA",
                "Se registró una compra al proveedor " + proveedor.getRazonSocial()
                        + " con comprobante " + tipoComprobante + " " + numeroComprobante
                        + " por un total de S/ " + redondear(total)
        );

        return "Compra registrada correctamente";
    }

    public List<CompraResponse> listarCompras() {
        return compraRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<CompraResponse> filtrarComprasPorFecha(LocalDate desde, LocalDate hasta) {
        return compraRepository.findByFechaBetween(desde, hasta)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CompraKpiResponse obtenerKpisCompras(LocalDate desde, LocalDate hasta) {

        List<Compra> compras = compraRepository.findByFechaBetween(desde, hasta)
                .stream()
                .filter(c -> c.getEstado() == null || c.getEstado() == EstadoCompra.REGISTRADA)
                .toList();

        double totalCompras = compras.stream()
                .mapToDouble(c -> c.getTotal() != null ? c.getTotal() : 0.0)
                .sum();

        int cantidadCompras = compras.size();

        double promedioPorCompra = cantidadCompras == 0
                ? 0
                : totalCompras / cantidadCompras;

        return new CompraKpiResponse(
                redondear(totalCompras),
                cantidadCompras,
                redondear(promedioPorCompra)
        );
    }

    public String anularCompra(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new RuntimeException("La compra ya se encuentra anulada");
        }

        compra.setEstado(EstadoCompra.ANULADA);

        compraRepository.save(compra);

        auditoriaService.registrar(
                "COMPRAS",
                "ANULAR COMPRA",
                "Se anuló la compra ID " + id
                        + " del proveedor " + compra.getProveedor().getRazonSocial()
        );

        return "Compra anulada correctamente";
    }

    private CompraResponse mapToResponse(Compra c) {
        return new CompraResponse(
                c.getId(),
                c.getFecha() != null ? c.getFecha().toString() : null,
                c.getTipoComprobante() != null ? c.getTipoComprobante() : "-",
                c.getNumeroComprobante() != null ? c.getNumeroComprobante() : "-",
                c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "-",
                c.getSubtotal() != null ? c.getSubtotal() : c.getTotal(),
                c.getIgv() != null ? c.getIgv() : 0.0,
                c.getTotal(),
                c.getObservacion() != null ? c.getObservacion() : "-",
                c.getEstado() != null ? c.getEstado().name() : "REGISTRADA",
                c.getDetalles() != null
                        ? c.getDetalles()
                        .stream()
                        .map(d -> new DetalleCompraResponse(
                                d.getNombreProducto(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()
                        ))
                        .toList()
                        : List.of()
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}