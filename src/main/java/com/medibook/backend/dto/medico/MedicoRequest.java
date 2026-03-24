package com.medibook.backend.dto.medico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// El Admin usa esto para crear el perfil de médico de un usuario existente.
// Flujo: Admin crea usuario → Admin crea perfil Medico vinculando ese usuario.
@Data
public class MedicoRequest {

    @NotBlank(message = "El CMP es requerido")
    private String cmp;           // Código de Matrícula Profesional

    private String telefono;

    @NotNull(message = "La especialidad es requerida")
    private Long especialidadId;

    @NotNull(message = "El usuario es requerido")
    private Long usuarioId;       // El Usuario ya debe existir con ROLE_MEDICO
}