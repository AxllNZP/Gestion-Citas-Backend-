package com.medibook.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO = Data Transfer Object
// Es el "contrato" que le dices al frontend: "esto es lo que debes enviarme"
// @Data de Lombok genera automáticamente getters, setters, equals, hashCode y toString
@Data
public class LoginRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}