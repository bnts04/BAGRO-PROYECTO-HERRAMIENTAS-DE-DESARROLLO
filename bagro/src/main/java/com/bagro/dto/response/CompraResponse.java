package com.bagro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CompraResponse {

    private Long id;
    private String fecha;
    private String tipoComprobante;
    private String numeroComprobante;
    private String proveedor;
    private Double subtotal;
    private Double igv;
    private Double total;
    private String observacion;
    private String estadoCompra;
    private List<DetalleCompraResponse> productos;
}