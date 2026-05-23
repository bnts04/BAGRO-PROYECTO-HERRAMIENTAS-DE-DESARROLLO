package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProveedorResponse {

    private Long id;
    private String ruc;
    private String razonSocial;
    private String tipoProducto;
    private String estado;
    private String observacion;
    private boolean activo;
}