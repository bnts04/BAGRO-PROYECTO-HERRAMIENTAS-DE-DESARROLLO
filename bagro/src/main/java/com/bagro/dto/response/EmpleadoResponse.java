package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmpleadoResponse {

    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String cargo;
    private String area;
    private Double sueldoBase;
    private boolean activo;
    private String username;
}