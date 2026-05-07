package com.bagro.service;

import com.bagro.dto.request.PagoRequest;
import com.bagro.dto.response.PagoResponse;
import com.bagro.entity.Pago;
import com.bagro.entity.Empleado;
import com.bagro.repository.PagoRepository;
import com.bagro.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final EmpleadoRepository empleadoRepository;

    public PagoService(PagoRepository pagoRepository, EmpleadoRepository empleadoRepository) {
        this.pagoRepository = pagoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public String crearPago(String username, PagoRequest request) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Double totalNeto = request.getSueldoBase()
                + (request.getHorasExtra() != null ? request.getHorasExtra() : 0)
                + (request.getBonos() != null ? request.getBonos() : 0)
                - (request.getDescuentos() != null ? request.getDescuentos() : 0);

        Pago pago = Pago.builder()
                .fecha(LocalDate.now())
                .sueldoBase(request.getSueldoBase())
                .horasExtra(request.getHorasExtra())
                .bonos(request.getBonos())
                .descuentos(request.getDescuentos())
                .totalNeto(totalNeto)
                .empleado(empleado)
                .build();

        pagoRepository.save(pago);

        return "Pago registrado correctamente";
    }

    public List<PagoResponse> listarPagos(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return pagoRepository.findByEmpleado(empleado)
                .stream()
                .map(p -> new PagoResponse(
                        p.getId(),
                        p.getFecha().toString(),
                        p.getSueldoBase(),
                        p.getHorasExtra(),
                        p.getBonos(),
                        p.getDescuentos(),
                        p.getTotalNeto(),
                        p.getEmpleado().getNombres() + " " + p.getEmpleado().getApellidos(),
                        p.getEmpleado().getDni()
                ))
                .toList();
    }
}