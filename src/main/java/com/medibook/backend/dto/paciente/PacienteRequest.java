package com.medibook.backend.dto.paciente;

import lombok.Data;
import java.util.Date;

// El paciente completa su perfil después de registrarse
@Data
public class PacienteRequest {
    private String telefono;
    private String direccion;
    private Date fechaNacimiento;
    private String grupoSanguineo; // Ej: "A+", "O-"
}