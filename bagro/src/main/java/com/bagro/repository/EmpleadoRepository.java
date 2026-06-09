package com.bagro.repository;

import com.bagro.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsByDni(String dni);

    Optional<Empleado> findByUserUsername(String username);

    Optional<Empleado> findByDni(String dni);

    List<Empleado> findByActivo(boolean activo);
}