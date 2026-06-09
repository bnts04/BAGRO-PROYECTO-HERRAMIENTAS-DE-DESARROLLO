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

    // Mes y año al que corresponde el pago
    private Integer mes;
    private Integer anio;

    private String dni;
}