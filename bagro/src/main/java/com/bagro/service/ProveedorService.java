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
    private final AuditoriaService auditoriaService;

    public ProveedorService(ProveedorRepository proveedorRepository,
                            SunatClient sunatClient,
                            AuditoriaService auditoriaService) {
        this.proveedorRepository = proveedorRepository;
        this.sunatClient = sunatClient;
        this.auditoriaService = auditoriaService;
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

        auditoriaService.registrar(
                "PROVEEDORES",
                "CREAR PROVEEDOR",
                "Se registró el proveedor " + proveedor.getRazonSocial()
                        + " con RUC " + proveedor.getRuc()
        );

        return "Proveedor registrado correctamente con datos de SUNAT";
    }

    public String editarProveedor(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedor.setTipoProducto(request.getTipoProducto());
        proveedor.setEstado(EstadoProveedor.valueOf(request.getEstado().toUpperCase()));
        proveedor.setObservacion(request.getObservacion());
        proveedor.setActivo(request.isActivo());

        proveedorRepository.save(proveedor);

        auditoriaService.registrar(
                "PROVEEDORES",
                "EDITAR PROVEEDOR",
                "Se editó el proveedor ID " + id
                        + " con RUC " + proveedor.getRuc()
                        + " - " + proveedor.getRazonSocial()
        );

        return "Proveedor actualizado correctamente";
    }

    public String desactivarProveedor(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedor.setActivo(false);

        proveedorRepository.save(proveedor);

        auditoriaService.registrar(
                "PROVEEDORES",
                "DESACTIVAR PROVEEDOR",
                "Se desactivó el proveedor ID " + id
                        + " con RUC " + proveedor.getRuc()
                        + " - " + proveedor.getRazonSocial()
        );

        return "Proveedor desactivado correctamente";
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

    public List<ProveedorResponse> filtrarProveedoresPorEstado(boolean activo) {
        return proveedorRepository.findByActivo(activo)
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