package com.medibook.backend.service;

import com.medibook.backend.dto.paciente.PacienteRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;
import java.util.List;

public interface PacienteService {
    PacienteResponse crearPerfil(Long usuarioId, PacienteRequest request);
    PacienteResponse obtenerPorId(Long id);
    PacienteResponse obtenerPorUsuarioId(Long usuarioId);
    List<PacienteResponse> listarTodos();
    PacienteResponse actualizar(Long id, PacienteRequest request);
}