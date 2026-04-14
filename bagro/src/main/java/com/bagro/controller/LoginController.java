package com.bagro.controller;

import com.bagro.dto.request.LoginRequest;
import com.bagro.dto.response.LoginResponse;
import com.bagro.service.LoginService;
import org.springframework.web.bind.annotation.*;
import com.bagro.dto.request.RegisterUserRequest;

@RestController
@RequestMapping
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterUserRequest request) {
        return loginService.registerUser(request);
    }
}