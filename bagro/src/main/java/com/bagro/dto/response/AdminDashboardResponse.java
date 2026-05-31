package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminDashboardResponse {

    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long usuariosInactivos;

    private Long totalTrabajadores;
    private Long trabajadoresActivos;
    private Long trabajadoresInactivos;

    private Long totalProveedores;
    private Long proveedoresActivos;
    private Long proveedoresInactivos;

    private Double totalPagosMes;
    private Double totalComprasMes;

    private Long solicitudesPendientes;
}