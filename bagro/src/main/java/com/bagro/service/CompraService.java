package com.bagro.service;

import com.bagro.dto.request.CompraRequest;
import com.bagro.dto.request.DetalleCompraRequest;
import com.bagro.dto.response.CompraResponse;
import com.bagro.dto.response.DetalleCompraResponse;
import com.bagro.entity.Compra;
import com.bagro.entity.DetalleCompra;
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

    public CompraService(CompraRepository compraRepository,
                         ProveedorRepository proveedorRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
    }

    public String crearCompra(CompraRequest request) {

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Compra compra = Compra.builder()
                .fecha(LocalDate.now())
                .proveedor(proveedor)
                .total(0.0)
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

        return "Compra registrada correctamente";
    }

    public List<CompraResponse> listarCompras() {

        return compraRepository.findAll()
                .stream()
                .map(c -> new CompraResponse(
                        c.getId(),
                        c.getFecha().toString(),
                        c.getProveedor().getRazonSocial(),
                        c.getTotal(),
                        c.getDetalles()
                                .stream()
                                .map(d -> new DetalleCompraResponse(
                                        d.getNombreProducto(),
                                        d.getCantidad(),
                                        d.getPrecioUnitario(),
                                        d.getSubtotal()
                                ))
                                .toList()
                ))
                .toList();
    }
}