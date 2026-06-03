package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompraRequest {

    private Long proveedorId;

    private String fechaCompra;

    private String tipoComprobante;

    private String numeroComprobante;

    private String observacion;

    private List<DetalleCompraRequest> productos;
}