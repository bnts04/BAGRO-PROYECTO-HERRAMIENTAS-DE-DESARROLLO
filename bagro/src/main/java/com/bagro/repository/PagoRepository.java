package com.bagro.repository;

import com.bagro.entity.Pago;
import com.bagro.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByEmpleado(Empleado empleado);
}