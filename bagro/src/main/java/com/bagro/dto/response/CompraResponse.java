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
    private String proveedor;
    private Double total;
    private List<DetalleCompraResponse> productos;
}