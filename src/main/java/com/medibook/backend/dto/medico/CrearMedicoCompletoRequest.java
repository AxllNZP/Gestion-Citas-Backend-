package com.medibook.backend.dto.medico;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearMedicoCompletoRequest {

    // Datos del Usuario
    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "Los apellidos son requeridos")
    private String apellidos;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    // Datos del Médico
    @NotBlank(message = "El CMP es requerido")
    private String cmp;

    private String telefono;

    @NotNull(message = "La especialidad es requerida")
    private Long especialidadId;
}