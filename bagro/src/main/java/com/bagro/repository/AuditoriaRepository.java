package com.bagro.repository;

import com.bagro.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();

    List<Auditoria> findByModuloOrderByFechaHoraDesc(String modulo);

    List<Auditoria> findByUsuarioOrderByFechaHoraDesc(String usuario);
}