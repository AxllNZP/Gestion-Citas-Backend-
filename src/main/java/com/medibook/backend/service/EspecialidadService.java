package com.medibook.backend.service;

import com.medibook.backend.dto.especialidad.EspecialidadRequest;
import com.medibook.backend.dto.especialidad.EspecialidadResponse;
import java.util.List;

public interface EspecialidadService {
    EspecialidadResponse crear(EspecialidadRequest request);
    List<EspecialidadResponse> listarTodas();
    EspecialidadResponse obtenerPorId(Long id);
    EspecialidadResponse actualizar(Long id, EspecialidadRequest request);
    void eliminar(Long id);
}