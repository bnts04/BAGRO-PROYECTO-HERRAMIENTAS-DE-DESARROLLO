package com.bagro.service;

import com.bagro.dto.response.AdminDashboardResponse;
import com.bagro.entity.Compra;
import com.bagro.entity.Empleado;
import com.bagro.entity.Pago;
import com.bagro.entity.Proveedor;
import com.bagro.entity.Solicitud;
import com.bagro.entity.User;
import com.bagro.repository.CompraRepository;
import com.bagro.repository.EmpleadoRepository;
import com.bagro.repository.PagoRepository;
import com.bagro.repository.ProveedorRepository;
import com.bagro.repository.SolicitudRepository;
import com.bagro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProveedorRepository proveedorRepository;
    private final PagoRepository pagoRepository;
    private final CompraRepository compraRepository;
    private final SolicitudRepository solicitudRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 EmpleadoRepository empleadoRepository,
                                 ProveedorRepository proveedorRepository,
                                 PagoRepository pagoRepository,
                                 CompraRepository compraRepository,
                                 SolicitudRepository solicitudRepository) {
        this.userRepository = userRepository;
        this.empleadoRepository = empleadoRepository;
        this.proveedorRepository = proveedorRepository;
        this.pagoRepository = pagoRepository;
        this.compraRepository = compraRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public AdminDashboardResponse obtenerResumenAdmin(int mes, int anio) {

        List<User> usuarios = userRepository.findAll();
        List<Empleado> trabajadores = empleadoRepository.findAll();
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<Pago> pagos = pagoRepository.findAll();
        List<Solicitud> solicitudes = solicitudRepository.findAll();

        YearMonth periodo = YearMonth.of(anio, mes);
        LocalDate desde = periodo.atDay(1);
        LocalDate hasta = periodo.atEndOfMonth();

        List<Compra> comprasMes = compraRepository.findByFechaBetween(desde, hasta);

        Long totalUsuarios = (long) usuarios.size();
        Long usuariosActivos = usuarios.stream().filter(User::isActive).count();
        Long usuariosInactivos = totalUsuarios - usuariosActivos;

        Long totalTrabajadores = (long) trabajadores.size();
        Long trabajadoresActivos = trabajadores.stream().filter(Empleado::isActivo).count();
        Long trabajadoresInactivos = totalTrabajadores - trabajadoresActivos;

        Long totalProveedores = (long) proveedores.size();
        Long proveedoresActivos = proveedores.stream().filter(Proveedor::isActivo).count();
        Long proveedoresInactivos = totalProveedores - proveedoresActivos;

        Double totalPagosMes = pagos.stream()
                .filter(p -> p.getMes() != null && p.getAnio() != null)
                .filter(p -> p.getMes() == mes && p.getAnio() == anio)
                .mapToDouble(Pago::getTotalNeto)
                .sum();

        Double totalComprasMes = comprasMes.stream()
                .mapToDouble(Compra::getTotal)
                .sum();

        Long solicitudesPendientes = solicitudes.stream()
                .filter(s -> s.getEstado() != null)
                .filter(s -> s.getEstado().name().equals("PENDIENTE"))
                .count();

        return new AdminDashboardResponse(
                totalUsuarios,
                usuariosActivos,
                usuariosInactivos,
                totalTrabajadores,
                trabajadoresActivos,
                trabajadoresInactivos,
                totalProveedores,
                proveedoresActivos,
                proveedoresInactivos,
                totalPagosMes,
                totalComprasMes,
                solicitudesPendientes
        );
    }
}