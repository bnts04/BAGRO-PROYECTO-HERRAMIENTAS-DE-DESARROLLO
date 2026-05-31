package com.bagro.service;

import com.bagro.dto.response.AsistenciaResponse;
import com.bagro.entity.Asistencia;
import com.bagro.entity.Empleado;
import com.bagro.entity.EstadoAsistencia;
import com.bagro.repository.AsistenciaRepository;
import com.bagro.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EmpleadoRepository empleadoRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                             EmpleadoRepository empleadoRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public String iniciarJornada(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDate hoy = LocalDate.now();

        if (asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy).isPresent()) {
            throw new RuntimeException("Ya inició su jornada hoy");
        }

        Asistencia asistencia = Asistencia.builder()
                .fecha(hoy)
                .horaEntrada(LocalTime.now())
                .estado(EstadoAsistencia.INICIADO)
                .empleado(empleado)
                .build();

        asistenciaRepository.save(asistencia);

        return "Jornada iniciada correctamente";
    }

    public String finalizarJornada(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDate hoy = LocalDate.now();

        Asistencia asistencia = asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy)
                .orElseThrow(() -> new RuntimeException("No ha iniciado jornada hoy"));

        if (asistencia.getHoraSalida() != null) {
            throw new RuntimeException("Ya finalizó su jornada hoy");
        }

        asistencia.setHoraSalida(LocalTime.now());
        asistencia.setEstado(EstadoAsistencia.FINALIZADO);

        asistenciaRepository.save(asistencia);

        return "Jornada finalizada correctamente";
    }

    public AsistenciaResponse miEstado(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        LocalDate hoy = LocalDate.now();

        Asistencia asistencia = asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy)
                .orElseThrow(() -> new RuntimeException("No tiene asistencia registrada hoy"));

        return new AsistenciaResponse(
                asistencia.getId(),
                asistencia.getFecha().toString(),
                asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada().toString() : null,
                asistencia.getHoraSalida() != null ? asistencia.getHoraSalida().toString() : null,
                asistencia.getEstado().name(),
                asistencia.getEmpleado().getNombres() + " " + asistencia.getEmpleado().getApellidos(),
                asistencia.getEmpleado().getDni()
        );
    }

    public List<AsistenciaResponse> listarAsistencias() {
        return asistenciaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(a -> new AsistenciaResponse(
                        a.getId(),
                        a.getFecha().toString(),
                        a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : null,
                        a.getHoraSalida() != null ? a.getHoraSalida().toString() : null,
                        a.getEstado().name(),
                        a.getEmpleado().getNombres() + " " + a.getEmpleado().getApellidos(),
                        a.getEmpleado().getDni()
                ))
                .toList();
    }

    public List<AsistenciaResponse> filtrarAsistenciasPorFecha(String fecha) {
        LocalDate fechaFiltro = LocalDate.parse(fecha);

        return asistenciaRepository.findByFechaOrderByIdDesc(fechaFiltro)
                .stream()
                .map(a -> new AsistenciaResponse(
                        a.getId(),
                        a.getFecha().toString(),
                        a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : null,
                        a.getHoraSalida() != null ? a.getHoraSalida().toString() : null,
                        a.getEstado().name(),
                        a.getEmpleado().getNombres() + " " + a.getEmpleado().getApellidos(),
                        a.getEmpleado().getDni()
                ))
                .toList();
    }
}