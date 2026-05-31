package com.bagro.repository;

import com.bagro.entity.Empleado;
import com.bagro.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByEmpleado(Empleado empleado);

    boolean existsByEmpleadoAndMesAndAnio(Empleado empleado, Integer mes, Integer anio);

    boolean existsByEmpleadoAndMesAndAnioAndIdNot(Empleado empleado, Integer mes, Integer anio, Long id);

    List<Pago> findByMesAndAnio(Integer mes, Integer anio);
}