package com.medibook.backend.dto.cita;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO exclusivo para finalizar una cita.
// Al separarlo de ActualizarEstadoCitaRequest mantenemos cada DTO
// con una sola responsabilidad — principio SOLID.
@Data
public class FinalizarCitaRequest {

    @NotBlank(message = "El diagnóstico u observaciones son requeridos")
    private String diagnostico;

    @NotBlank(message = "Las indicaciones son requeridas")
    private String indicaciones;
}