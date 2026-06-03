package com.bagro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 50)
    private String tipoComprobante;

    @Column(length = 50)
    private String numeroComprobante;

    @Column(length = 1000)
    private String observacion;

    private Double subtotal;

    private Double igv;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles;
}