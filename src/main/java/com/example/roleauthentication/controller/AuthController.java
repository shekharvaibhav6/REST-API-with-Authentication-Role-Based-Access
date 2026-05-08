package com.example.roleauthentication.controller;

import com.example.roleauthentication.dto.Dto;
import com.example.roleauthentication.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login and profile APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<Dto.ApiResponse<Dto.AuthResponse>> register(
            @Valid @RequestBody Dto.RegisterRequest req) {

        Dto.AuthResponse authResponse = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Dto.ApiResponse.ok("Registration successful", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<Dto.ApiResponse<Dto.AuthResponse>> login(
            @Valid @RequestBody Dto.LoginRequest req) {

        Dto.AuthResponse authResponse = authService.login(req);
        return ResponseEntity.ok(Dto.ApiResponse.ok("Login successful", authResponse));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<Dto.ApiResponse<Dto.UserResponse>> getProfile(
            @AuthenticationPrincipal String email) {

        return ResponseEntity.ok(Dto.ApiResponse.ok(authService.getProfile(email)));
    }
}