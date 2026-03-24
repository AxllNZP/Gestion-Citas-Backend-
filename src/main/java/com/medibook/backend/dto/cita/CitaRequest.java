package com.medibook.backend.dto.cita;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

// Lo que el PACIENTE envía para agendar una cita
// Nota: NO incluimos pacienteId aquí — lo sacamos del token JWT por seguridad.
// Un paciente nunca podría agendar citas "en nombre" de otro.
@Data
public class CitaRequest {

    @NotNull(message = "La fecha y hora son requeridas")
    @Future(message = "La cita debe ser en una fecha futura")  // Validación automática: no agendar en el pasado
    private LocalDateTime fechaHora;

    private String motivo; // Opcional

    @NotNull(message = "Debe seleccionar un médico")
    private Long medicoId;
}