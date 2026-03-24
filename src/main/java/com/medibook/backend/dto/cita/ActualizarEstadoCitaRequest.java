package com.medibook.backend.dto.cita;

import com.medibook.backend.model.EstadoCita;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Usado por médicos y admin para cambiar el estado de una cita
// (PENDIENTE → CONFIRMADA, CANCELADA, FINALIZADA)
@Data
public class ActualizarEstadoCitaRequest {

    @NotNull(message = "El estado es requerido")
    private EstadoCita estado;
}