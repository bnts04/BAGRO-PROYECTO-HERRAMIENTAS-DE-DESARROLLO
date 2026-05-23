package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalleCompraRequest {

    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
}