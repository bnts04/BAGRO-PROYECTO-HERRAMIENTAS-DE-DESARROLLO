package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoRequest {

    private String dni;
    private String nombres;
    private String apellidos;
    private String cargo;
    private String area;
    private Double sueldoBase;
}