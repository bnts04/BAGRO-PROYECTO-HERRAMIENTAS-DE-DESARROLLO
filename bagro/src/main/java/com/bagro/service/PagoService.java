package com.bagro.service;

import com.bagro.dto.request.PagoRequest;
import com.bagro.dto.response.PagoResponse;
import com.bagro.dto.response.PlanillaKpiResponse;
import com.bagro.entity.Empleado;
import com.bagro.entity.EstadoPago;
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
    private final AuditoriaService auditoriaService;

    public PagoService(PagoRepository pagoRepository,
                       EmpleadoRepository empleadoRepository,
                       AuditoriaService auditoriaService) {
        this.pagoRepository = pagoRepository;
        this.empleadoRepository = empleadoRepository;
        this.auditoriaService = auditoriaService;
    }

    public String crearPago(String username, PagoRequest request) {

        if (request.getDni() == null || request.getDni().isBlank()) {
            throw new RuntimeException("Debe ingresar el DNI del trabajador");
        }

        Empleado empleado = empleadoRepository.findByDni(request.getDni())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        if (!empleado.isActivo()) {
            throw new RuntimeException("No se puede registrar pago a un trabajador inactivo");
        }

        LocalDate hoy = LocalDate.now();

        Integer mesPago = request.getMes() != null ? request.getMes() : hoy.getMonthValue();
        Integer anioPago = request.getAnio() != null ? request.getAnio() : hoy.getYear();

        if (pagoRepository.existsByEmpleadoAndMesAndAnio(empleado, mesPago, anioPago)) {
            throw new RuntimeException("Ya existe un pago registrado para este trabajador en ese mes y año");
        }

        Double sueldoBase = request.getSueldoBase() != null
                ? request.getSueldoBase()
                : empleado.getSueldoBase();

        if (sueldoBase == null) {
            throw new RuntimeException("El trabajador no tiene sueldo base registrado");
        }

        Double horasExtra = request.getHorasExtra() != null ? request.getHorasExtra() : 0.0;
        Double bonos = request.getBonos() != null ? request.getBonos() : 0.0;
        Double descuentos = request.getDescuentos() != null ? request.getDescuentos() : 0.0;

        Double totalNeto = sueldoBase + horasExtra + bonos - descuentos;

        Pago pago = Pago.builder()
                .fecha(hoy)
                .mes(mesPago)
                .anio(anioPago)
                .sueldoBase(sueldoBase)
                .horasExtra(horasExtra)
                .bonos(bonos)
                .descuentos(descuentos)
                .totalNeto(totalNeto)
                .estado(EstadoPago.PAGADO)
                .empleado(empleado)
                .build();

        pagoRepository.save(pago);

        auditoriaService.registrar(
                "PAGOS",
                "REGISTRAR PAGO",
                "El usuario " + username + " registró un pago para el trabajador DNI " + empleado.getDni()
                        + " correspondiente al mes " + mesPago + "/" + anioPago
                        + " por un total neto de S/ " + totalNeto
        );

        return "Pago registrado correctamente";
    }

    public String editarPago(Long id, PagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        Empleado empleado = pago.getEmpleado();

        Integer mesPago = request.getMes() != null ? request.getMes() : pago.getMes();
        Integer anioPago = request.getAnio() != null ? request.getAnio() : pago.getAnio();

        if (pagoRepository.existsByEmpleadoAndMesAndAnioAndIdNot(empleado, mesPago, anioPago, id)) {
            throw new RuntimeException("Ya existe otro pago registrado para este trabajador en ese mes y año");
        }

        Double sueldoBase = request.getSueldoBase();
        Double horasExtra = request.getHorasExtra() != null ? request.getHorasExtra() : 0;
        Double bonos = request.getBonos() != null ? request.getBonos() : 0;
        Double descuentos = request.getDescuentos() != null ? request.getDescuentos() : 0;

        Double totalNeto = sueldoBase + horasExtra + bonos - descuentos;

        pago.setMes(mesPago);
        pago.setAnio(anioPago);
        pago.setSueldoBase(sueldoBase);
        pago.setHorasExtra(horasExtra);
        pago.setBonos(bonos);
        pago.setDescuentos(descuentos);
        pago.setTotalNeto(totalNeto);

        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PAGADO);
        }

        pagoRepository.save(pago);

        auditoriaService.registrar(
                "PAGOS",
                "EDITAR PAGO",
                "Se editó el pago ID " + id
                        + " del trabajador DNI " + empleado.getDni()
                        + " correspondiente al mes " + mesPago + "/" + anioPago
        );

        return "Pago actualizado correctamente";
    }

    public List<PagoResponse> listarPagos(String username) {
        Empleado empleado = empleadoRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        return pagoRepository.findByEmpleado(empleado)
                .stream()
                .map(this::mapToResponse)
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

    public List<PagoResponse> filtrarPagosPorMesAnio(Integer mes, Integer anio) {
        return pagoRepository.findByMesAndAnio(mes, anio)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PagoResponse mapToResponse(Pago p) {
        return new PagoResponse(
                p.getId(),
                p.getFecha().toString(),
                p.getSueldoBase(),
                p.getHorasExtra(),
                p.getBonos(),
                p.getDescuentos(),
                p.getTotalNeto(),
                p.getEmpleado().getNombres() + " " + p.getEmpleado().getApellidos(),
                p.getEmpleado().getDni(),
                p.getEstado() != null ? p.getEstado().name() : "PAGADO"
        );
    }

    public String anularPago(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.ANULADO);

        pagoRepository.save(pago);

        auditoriaService.registrar(
                "PAGOS",
                "ANULAR PAGO",
                "Se anuló el pago ID " + id
                        + " del trabajador DNI " + pago.getEmpleado().getDni()
        );

        return "Pago anulado correctamente";
    }
}