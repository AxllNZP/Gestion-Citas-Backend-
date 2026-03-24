package com.medibook.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// Este es el DTO de RESPUESTA: lo que el backend le devuelve al frontend
// al hacer login o registro exitoso.
// @Builder permite crear el objeto de forma legible: AuthResponse.builder().token("...").build()
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;    // El JWT que el frontend guardará y enviará en cada request
    private String tipo;     // Siempre "Bearer" - es el estándar HTTP para JWT

    // Datos del usuario para que el frontend pueda mostrarlos sin hacer otra petición
    private Long id;
    private String email;
    private String nombre;
    private String apellidos;
    private Set<String> roles; // Ej: ["ROLE_PACIENTE"] - para el frontend saber qué mostrar
}