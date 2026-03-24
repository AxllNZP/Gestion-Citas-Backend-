package com.medibook.backend.service;

import com.medibook.backend.dto.medico.CrearMedicoCompletoRequest;
import com.medibook.backend.dto.medico.MedicoRequest;
import com.medibook.backend.dto.medico.MedicoResponse;
import java.util.List;

public interface MedicoService {
    MedicoResponse crear(MedicoRequest request);
    List<MedicoResponse> listarTodos();
    List<MedicoResponse> listarPorEspecialidad(String especialidad);
    MedicoResponse obtenerPorId(Long id);
    MedicoResponse obtenerPorEmail(String email);  // usado en Controller para obtener médico del token
    MedicoResponse actualizarTelefono(Long id, String telefono);
    MedicoResponse crearCompleto(CrearMedicoCompletoRequest request);
    MedicoResponse actualizar(Long id, MedicoRequest request);
    void eliminar(Long id);
    MedicoResponse actualizarPorEmail(String email, MedicoRequest request);
}