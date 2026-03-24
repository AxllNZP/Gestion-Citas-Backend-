package com.medibook.backend.dto.especialidad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EspecialidadRequest {
    @NotBlank(message = "El nombre de la especialidad es requerido")
    private String nombre;
    private String descripcion;
}