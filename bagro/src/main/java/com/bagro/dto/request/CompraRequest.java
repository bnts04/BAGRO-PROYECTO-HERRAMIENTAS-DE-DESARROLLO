package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompraRequest {

    private Long proveedorId;

    private List<DetalleCompraRequest> productos;
}