package com.medibook.backend.service;

import com.medibook.backend.dto.auth.AuthResponse;
import com.medibook.backend.dto.auth.LoginRequest;
import com.medibook.backend.dto.auth.RegisterRequest;

// La interfaz define el CONTRATO del servicio.
// Beneficio: el Controller depende de esta interfaz, no de la implementación.
// Esto permite hacer tests con mocks y cambiar implementaciones sin tocar el Controller.
public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    //CREAR ADMINISTRADOR DE SER NECEASRIO
    AuthResponse crearAdmin(RegisterRequest request);
}