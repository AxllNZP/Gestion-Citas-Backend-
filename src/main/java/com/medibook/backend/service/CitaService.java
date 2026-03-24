package com.medibook.backend.service;

import com.medibook.backend.dto.cita.ActualizarEstadoCitaRequest;
import com.medibook.backend.dto.cita.CitaRequest;
import com.medibook.backend.dto.cita.CitaResponse;
import com.medibook.backend.dto.cita.FinalizarCitaRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;

import java.util.List;

public interface CitaService {
    CitaResponse crearCita(Long pacienteUsuarioId, CitaRequest request);
    List<CitaResponse> obtenerCitasPorPaciente(Long pacienteId);
    List<CitaResponse> obtenerCitasPorMedico(Long medicoId);
    List<CitaResponse> listarTodas();
    CitaResponse actualizarEstado(Long citaId, ActualizarEstadoCitaRequest request);
    void cancelarCita(Long citaId, String emailUsuario);  // solo el dueño o admin puede cancelar
    CitaResponse finalizarCita(Long citaId, FinalizarCitaRequest request);

    // Obtener pacientes atendidos por medicos y historial medico.
    List<PacienteResponse> obtenerPacientesAtendidosPorMedico(Long medicoId);
    List<CitaResponse> obtenerHistorialPaciente(Long medicoId, Long pacienteId);

    //Historial completo del paciente
    List<CitaResponse> obtenerHistorialCompletoPaciente(Long pacienteId);
}