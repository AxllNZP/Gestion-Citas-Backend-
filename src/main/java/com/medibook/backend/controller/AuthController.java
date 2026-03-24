package com.medibook.backend.controller;

import com.medibook.backend.dto.auth.AuthResponse;
import com.medibook.backend.dto.auth.LoginRequest;
import com.medibook.backend.dto.auth.RegisterRequest;
import com.medibook.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// El Controller es DELGADO: no tiene lógica de negocio.
// Solo: recibe request → llama al service → devuelve respuesta.
// @Valid activa las validaciones definidas en los DTOs (@NotBlank, @Email, etc.)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/register  (solo crea PACIENTES)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // POST /api/auth/crear-admin  — Solo ADMIN puede crear otros admins
    @PostMapping("/crear-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> crearAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.crearAdmin(request));
    }
}