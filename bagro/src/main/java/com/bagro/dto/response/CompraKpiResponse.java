package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CompraKpiResponse {

    private Double totalCompras;
    private Integer cantidadCompras;
    private Double promedioPorCompra;
}