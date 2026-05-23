package com.bagro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ruc;

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String tipoProducto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProveedor estado;

    private String observacion;

    @Column(nullable = false)
    private boolean activo;
}