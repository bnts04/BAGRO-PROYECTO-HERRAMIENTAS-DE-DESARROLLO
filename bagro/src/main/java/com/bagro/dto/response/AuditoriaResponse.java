package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuditoriaResponse {

    private Long id;
    private String usuario;
    private String modulo;
    private String accion;
    private String descripcion;
    private String fechaHora;
}