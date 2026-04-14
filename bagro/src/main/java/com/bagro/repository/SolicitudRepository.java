package com.bagro.repository;

import com.bagro.entity.Empleado;
import com.bagro.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEmpleadoOrderByIdDesc(Empleado empleado);

    List<Solicitud> findAllByOrderByIdDesc();
}