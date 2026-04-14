package com.bagro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private Double sueldoBase;

    @Column(nullable = false)
    private boolean activo;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}