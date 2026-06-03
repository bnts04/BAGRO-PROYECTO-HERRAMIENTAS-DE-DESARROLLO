package com.bagro.repository;

import com.bagro.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByFechaBetween(LocalDate desde, LocalDate hasta);

    List<Compra> findAllByOrderByFechaDesc();

    boolean existsByTipoComprobanteAndNumeroComprobante(String tipoComprobante, String numeroComprobante);
}