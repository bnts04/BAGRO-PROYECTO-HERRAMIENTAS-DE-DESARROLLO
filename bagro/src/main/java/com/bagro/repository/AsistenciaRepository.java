package com.bagro.repository;

import com.bagro.entity.Asistencia;
import com.bagro.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);

    List<Asistencia> findAllByOrderByFechaDesc();

    List<Asistencia> findByEmpleadoAndFechaBetween(Empleado empleado, LocalDate desde, LocalDate hasta);
}