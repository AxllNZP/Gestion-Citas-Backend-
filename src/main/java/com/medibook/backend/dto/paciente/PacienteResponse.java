package com.medibook.backend.dto.paciente;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class PacienteResponse {
    private Long id;
    private String telefono;
    private String direccion;
    private Date fechaNacimiento;
    private String grupoSanguineo;

    // Datos del Usuario vinculado (aplanados)
    private Long usuarioId;
    private String nombre;
    private String apellidos;
    private String email;
}