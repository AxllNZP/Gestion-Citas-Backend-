package com.medibook.backend.dto.cita;

import com.medibook.backend.model.EstadoCita;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// Lo que el backend devuelve cuando se consultan citas.
// En lugar de exponer la entidad completa (con todas sus relaciones),
// devolvemos solo lo necesario. Esto es el patrón DTO.
@Data
@Builder
public class CitaResponse {
    private Long id;
    private LocalDateTime fechaHora;
    private String motivo;
    private EstadoCita estado;

    // En vez de devolver objetos anidados complejos, devolvemos datos "aplanados"
    private Long pacienteId;
    private String pacienteNombre;     // nombre + apellidos del paciente

    private Long medicoId;
    private String medicoNombre;       // nombre + apellidos del médico
    private String especialidad;       // nombre de la especialidad
    // Agregacion de diagnostico e indicaciones
    private String diagnostico;
    private String indicaciones;
}