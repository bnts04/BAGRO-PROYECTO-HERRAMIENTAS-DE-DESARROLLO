package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SolicitudResponse {

    private Long id;
    private String tipo;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
    private String estado;
    private String comentario;
    private String empleado;
    private String dni;
}