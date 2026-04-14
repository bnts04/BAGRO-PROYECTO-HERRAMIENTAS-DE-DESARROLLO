package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudRevisionRequest {

    private String estado;
    private String comentario;
}