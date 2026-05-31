package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagoResponse {

    private Long id;
    private String fecha;
    private Double sueldoBase;
    private Double horasExtra;
    private Double bonos;
    private Double descuentos;
    private Double totalNeto;
    private String empleado;
    private String dni;
    private String estadoPago;
}