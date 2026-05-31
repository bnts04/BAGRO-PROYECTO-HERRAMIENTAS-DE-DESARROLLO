package com.bagro.controller;

import com.bagro.dto.response.AdminDashboardResponse;
import com.bagro.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse obtenerDashboardAdmin(
            @RequestParam int mes,
            @RequestParam int anio
    ) {
        return adminDashboardService.obtenerResumenAdmin(mes, anio);
    }
}