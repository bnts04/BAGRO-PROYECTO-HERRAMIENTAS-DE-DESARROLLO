package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudRequest {

    private String tipo;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
}