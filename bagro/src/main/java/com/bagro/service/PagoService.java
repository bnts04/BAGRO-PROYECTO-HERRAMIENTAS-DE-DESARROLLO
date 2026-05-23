package com.bagro.service;

import com.bagro.dto.request.PagoRequest;
import com.bagro.dto.response.PagoResponse;
import com.bagro.dto.response.PlanillaKpiResponse;
import com.bagro.entity.Empleado;
import com.bagro.entity.Pago;
import com.bagro.repository.EmpleadoRepository;
import com.bagro.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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

        LocalDate hoy = LocalDate.now();

        Integer mesPago = request.getMes() != null ? request.getMes() : hoy.getMonthValue();
        Integer anioPago = request.getAnio() != null ? request.getAnio() : hoy.getYear();

        Double totalNeto = request.getSueldoBase()
                + (request.getHorasExtra() != null ? request.getHorasExtra() : 0)
                + (request.getBonos() != null ? request.getBonos() : 0)
                - (request.getDescuentos() != null ? request.getDescuentos() : 0);

        Pago pago = Pago.builder()
                .fecha(hoy)
                .mes(mesPago)
                .anio(anioPago)
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

    public PlanillaKpiResponse obtenerKpisPlanilla(int mes, int anio) {
        List<Pago> pagos = pagoRepository.findAll();

        List<Pago> pagosMes = pagos.stream()
                .filter(p -> p.getMes() != null && p.getAnio() != null)
                .filter(p -> p.getMes() == mes && p.getAnio() == anio)
                .toList();

        double totalMes = pagosMes.stream()
                .mapToDouble(Pago::getTotalNeto)
                .sum();

        YearMonth mesAnterior = YearMonth.of(anio, mes).minusMonths(1);

        double totalMesAnterior = pagos.stream()
                .filter(p -> p.getMes() != null && p.getAnio() != null)
                .filter(p -> p.getMes() == mesAnterior.getMonthValue()
                        && p.getAnio() == mesAnterior.getYear())
                .mapToDouble(Pago::getTotalNeto)
                .sum();

        int trabajadoresActivos = (int) empleadoRepository.findAll()
                .stream()
                .filter(Empleado::isActivo)
                .count();

        double costoPromedio = trabajadoresActivos == 0 ? 0 : totalMes / trabajadoresActivos;

        double variacion = totalMesAnterior == 0
                ? 0
                : ((totalMes - totalMesAnterior) / totalMesAnterior) * 100;

        int totalPagos = pagosMes.size();

        int pagosATiempo = (int) pagosMes.stream()
                .filter(p -> p.getFecha().getMonthValue() == p.getMes()
                        && p.getFecha().getYear() == p.getAnio())
                .count();

        int pagosFueraDeTiempo = totalPagos - pagosATiempo;

        double porcentajePagosATiempo = totalPagos == 0
                ? 0
                : (pagosATiempo * 100.0) / totalPagos;

        return new PlanillaKpiResponse(
                BigDecimal.valueOf(totalMes).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(costoPromedio).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(variacion).setScale(2, RoundingMode.HALF_UP),
                porcentajePagosATiempo,
                trabajadoresActivos,
                totalPagos,
                pagosATiempo,
                pagosFueraDeTiempo
        );
    }
}