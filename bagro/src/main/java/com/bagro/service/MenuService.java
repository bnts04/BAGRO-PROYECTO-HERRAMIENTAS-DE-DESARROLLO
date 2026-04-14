package com.bagro.service;

import com.bagro.dto.response.MenuResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    public MenuResponse getMenuByRole(String role) {

        List<String> options = switch (role) {
            case "ADMIN" -> List.of(
                    "Gestión de usuarios",
                    "Gestión de trabajadores",
                    "Pagos",
                    "Compras",
                    "Reportes"
            );
            case "RRHH" -> List.of(
                    "Trabajadores",
                    "Asistencia",
                    "Solicitudes",
                    "Pagos"
            );
            case "TRABAJADOR" -> List.of(
                    "Mi perfil",
                    "Marcar asistencia",
                    "Solicitudes",
                    "Historial de pagos"
            );
            case "COMPRAS" -> List.of(
                    "Proveedores",
                    "Compras",
                    "Historial de compras"
            );
            default -> List.of();
        };

        return new MenuResponse(role, options);
    }
}