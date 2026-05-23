package com.bagro.service;

import com.bagro.dto.external.SunatRucResponse;
import com.bagro.dto.request.ProveedorRequest;
import com.bagro.dto.response.ProveedorResponse;
import com.bagro.entity.EstadoProveedor;
import com.bagro.entity.Proveedor;
import com.bagro.integration.SunatClient;
import com.bagro.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final SunatClient sunatClient;

    public ProveedorService(ProveedorRepository proveedorRepository,
                            SunatClient sunatClient) {
        this.proveedorRepository = proveedorRepository;
        this.sunatClient = sunatClient;
    }

    public String crearProveedor(ProveedorRequest request) {

        if (proveedorRepository.existsByRuc(request.getRuc())) {
            throw new RuntimeException("El RUC ya existe");
        }

        SunatRucResponse datosSunat = sunatClient.consultarPorRuc(request.getRuc());

        if (datosSunat == null || datosSunat.razon_social() == null) {
            throw new RuntimeException("No se pudo validar el RUC en SUNAT");
        }

        Proveedor proveedor = Proveedor.builder()
                .ruc(request.getRuc())
                .razonSocial(datosSunat.razon_social())
                .tipoProducto(request.getTipoProducto())
                .estado(EstadoProveedor.valueOf(request.getEstado().toUpperCase()))
                .observacion(request.getObservacion())
                .activo(request.isActivo())
                .build();

        proveedorRepository.save(proveedor);

        return "Proveedor registrado correctamente con datos de SUNAT";
    }

    public List<ProveedorResponse> listarProveedores() {

        return proveedorRepository.findAll()
                .stream()
                .map(p -> new ProveedorResponse(
                        p.getId(),
                        p.getRuc(),
                        p.getRazonSocial(),
                        p.getTipoProducto(),
                        p.getEstado().name(),
                        p.getObservacion(),
                        p.isActivo()
                ))
                .toList();
    }
}