package com.bagro.dto.external;

public record SunatRucResponse(
        String razon_social,
        String numero_documento,
        String estado,
        String condicion,
        String direccion,
        String distrito,
        String provincia,
        String departamento
) {
}