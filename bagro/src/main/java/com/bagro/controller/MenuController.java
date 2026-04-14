package com.bagro.controller;

import com.bagro.dto.response.MenuResponse;
import com.bagro.service.MenuService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/api/menu")
    public MenuResponse getMenu(Authentication authentication) {
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"))
                .getAuthority()
                .replace("ROLE_", "");

        return menuService.getMenuByRole(role);
    }
}