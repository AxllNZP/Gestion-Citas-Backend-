package com.medibook.backend.dto.medico;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicoResponse {
    private Long id;
    private String cmp;
    private String telefono;

    // Datos del Usuario vinculado (aplanados)
    private Long usuarioId;
    private String nombre;
    private String apellidos;
    private String email;

    // Datos de la Especialidad (aplanados)
    private Long especialidadId;
    private String especialidadNombre;
}