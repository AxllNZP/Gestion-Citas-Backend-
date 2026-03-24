package com.medibook.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    private String email;

    // @Size valida la longitud - contraseña mínimo 8 caracteres
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    // El registro público SIEMPRE crea PACIENTE.
    // Los médicos los crea el Admin con otro endpoint.
    // Esto es una decisión de SEGURIDAD: nadie puede auto-asignarse rol ADMIN o MEDICO.
}