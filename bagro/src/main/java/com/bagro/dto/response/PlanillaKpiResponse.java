package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PlanillaKpiResponse {

    private BigDecimal totalPlanillaMensual;
    private BigDecimal costoPromedioPorTrabajador;
    private BigDecimal variacionMensual;
    private Double porcentajePagosATiempo;
    private Integer trabajadoresActivos;
    private Integer totalPagos;
    private Integer pagosATiempo;
    private Integer pagosFueraDeTiempo;
}