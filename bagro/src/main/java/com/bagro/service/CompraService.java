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

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Compra compra = Compra.builder()
                .fecha(LocalDate.now())
                .proveedor(proveedor)
                .total(0.0)
                .estado(EstadoCompra.REGISTRADA)
                .build();

        List<DetalleCompra> detalles = new ArrayList<>();

        double total = 0;

        for (DetalleCompraRequest item : request.getProductos()) {

            double subtotal = item.getCantidad() * item.getPrecioUnitario();

            DetalleCompra detalle = DetalleCompra.builder()
                    .nombreProducto(item.getNombreProducto())
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .subtotal(subtotal)
                    .compra(compra)
                    .build();

            detalles.add(detalle);
            total += subtotal;
        }

        compra.setDetalles(detalles);
        compra.setTotal(total);

        compraRepository.save(compra);

        auditoriaService.registrar(
                "COMPRAS",
                "REGISTRAR COMPRA",
                "Se registró una compra al proveedor " + proveedor.getRazonSocial()
                        + " por un total de S/ " + total
        );

        return "Compra registrada correctamente";
    }

    public List<CompraResponse> listarCompras() {
        return compraRepository.findAll()
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
                .mapToDouble(Compra::getTotal)
                .sum();

        int cantidadCompras = compras.size();

        double promedioPorCompra = cantidadCompras == 0
                ? 0
                : totalCompras / cantidadCompras;

        return new CompraKpiResponse(
                totalCompras,
                cantidadCompras,
                promedioPorCompra
        );
    }

    public String anularCompra(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

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
                c.getFecha().toString(),
                c.getProveedor().getRazonSocial(),
                c.getTotal(),
                c.getEstado() != null ? c.getEstado().name() : "REGISTRADA",
                c.getDetalles()
                        .stream()
                        .map(d -> new DetalleCompraResponse(
                                d.getNombreProducto(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()
                        ))
                        .toList()
        );
    }
}