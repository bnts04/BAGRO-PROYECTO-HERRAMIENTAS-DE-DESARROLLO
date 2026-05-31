package com.bagro.service;

import com.bagro.dto.response.AuditoriaResponse;
import com.bagro.entity.Auditoria;
import com.bagro.repository.AuditoriaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(String modulo, String accion, String descripcion) {
        String usuario = "SISTEMA";

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        Auditoria auditoria = Auditoria.builder()
                .usuario(usuario)
                .modulo(modulo)
                .accion(accion)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .build();

        auditoriaRepository.save(auditoria);
    }

    public List<AuditoriaResponse> listarAuditorias() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditoriaResponse> filtrarPorModulo(String modulo) {
        return auditoriaRepository.findByModuloOrderByFechaHoraDesc(modulo)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AuditoriaResponse> filtrarPorUsuario(String usuario) {
        return auditoriaRepository.findByUsuarioOrderByFechaHoraDesc(usuario)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditoriaResponse mapToResponse(Auditoria a) {
        return new AuditoriaResponse(
                a.getId(),
                a.getUsuario(),
                a.getModulo(),
                a.getAccion(),
                a.getDescripcion(),
                a.getFechaHora().toString()
        );
    }
}