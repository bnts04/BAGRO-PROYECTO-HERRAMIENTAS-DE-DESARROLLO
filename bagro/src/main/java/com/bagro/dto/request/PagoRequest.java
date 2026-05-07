package com.bagro.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoRequest {

    private Double sueldoBase;
    private Double horasExtra;
    private Double bonos;
    private Double descuentos;
}