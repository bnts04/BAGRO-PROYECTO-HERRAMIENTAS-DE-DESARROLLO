package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProveedorRequest {

    private String ruc;
    private String razonSocial;
    private String tipoProducto;
    private String estado;
    private String observacion;
    private boolean activo;
}