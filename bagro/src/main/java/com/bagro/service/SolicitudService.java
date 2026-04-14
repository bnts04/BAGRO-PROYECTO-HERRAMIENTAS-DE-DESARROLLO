package com.bagro.service;

import com.bagro.dto.request.SolicitudRequest;
import com.bagro.dto.request.SolicitudRevisionRequest;
import com.bagro.dto.response.SolicitudResponse;
import com.bagro.entity.Empleado;
import com.bagro.entity.EstadoSolicitud;
import com.bagro.entity.Solicitud;
import com.bagro.repository.EmpleadoRepository;
import com.bagro.repository.SolicitudRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final EmpleadoRepository empleadoRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            EmpleadoRepository empleadoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public String crearSolicitud(String username, SolicitudRequest request) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDate fechaInicio = LocalDate.parse(request.getFechaInicio());
        LocalDate fechaFin = LocalDate.parse(request.getFechaFin());
        LocalDate hoy = LocalDate.now();

        if (fechaInicio.isBefore(hoy) || fechaFin.isBefore(hoy)) {
            throw new RuntimeException("No se permiten fechas pasadas");
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new RuntimeException("La fecha fin no puede ser menor que la fecha inicio");
        }

        Solicitud solicitud = Solicitud.builder()
                .tipo(request.getTipo())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .descripcion(request.getDescripcion())
                .estado(EstadoSolicitud.PENDIENTE)
                .comentario(null)
                .empleado(empleado)
                .build();

        solicitudRepository.save(solicitud);

        return "Solicitud registrada correctamente";
    }

    public List<SolicitudResponse> misSolicitudes(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return solicitudRepository.findByEmpleadoOrderByIdDesc(empleado)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<SolicitudResponse> listarSolicitudes() {
        return solicitudRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public String revisarSolicitud(Long id, SolicitudRevisionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(request.getEstado().toUpperCase());

        if (nuevoEstado == EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("No se puede volver a estado PENDIENTE");
        }

        solicitud.setEstado(nuevoEstado);
        solicitud.setComentario(request.getComentario());

        solicitudRepository.save(solicitud);

        return "Solicitud actualizada correctamente";
    }

    private SolicitudResponse mapToResponse(Solicitud s) {
        return new SolicitudResponse(
                s.getId(),
                s.getTipo(),
                s.getFechaInicio().toString(),
                s.getFechaFin().toString(),
                s.getDescripcion(),
                s.getEstado().name(),
                s.getComentario(),
                s.getEmpleado().getNombres() + " " + s.getEmpleado().getApellidos(),
                s.getEmpleado().getDni()
        );
    }
}