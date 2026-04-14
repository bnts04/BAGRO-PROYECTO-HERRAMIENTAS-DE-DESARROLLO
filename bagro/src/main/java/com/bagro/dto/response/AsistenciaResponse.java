package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AsistenciaResponse {

    private Long id;
    private String fecha;
    private String horaEntrada;
    private String horaSalida;
    private String estado;
    private String empleado;
    private String dni;
}