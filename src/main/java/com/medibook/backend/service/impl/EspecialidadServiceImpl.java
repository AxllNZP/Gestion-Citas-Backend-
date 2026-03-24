package com.medibook.backend.service.impl;

import com.medibook.backend.dto.especialidad.EspecialidadRequest;
import com.medibook.backend.dto.especialidad.EspecialidadResponse;
import com.medibook.backend.exception.ConflictException;
import com.medibook.backend.exception.ResourceNotFoundException;
import com.medibook.backend.model.Especialidad;
import com.medibook.backend.repository.EspecialidadRepository;
import com.medibook.backend.service.EspecialidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    // Método privado de conversión entidad → DTO de respuesta
    // El patrón "toResponse" evita repetir la misma lógica de mapeo en cada método
    private EspecialidadResponse toResponse(Especialidad e) {
        return EspecialidadResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .build();
    }

    @Override
    public EspecialidadResponse crear(EspecialidadRequest request) {
        // Verificamos unicidad antes de guardar
        especialidadRepository.findByNombre(request.getNombre()).ifPresent(e -> {
            throw new ConflictException("Ya existe una especialidad con ese nombre: " + request.getNombre());
        });

        Especialidad especialidad = Especialidad.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();

        return toResponse(especialidadRepository.save(especialidad));
    }

    @Override
    public List<EspecialidadResponse> listarTodas() {
        return especialidadRepository.findAll().stream()
                .map(this::toResponse)   // this::toResponse es referencia a método — equivale a e -> toResponse(e)
                .collect(Collectors.toList());
    }

    @Override
    public EspecialidadResponse obtenerPorId(Long id) {
        try {
            return toResponse(especialidadRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id)));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow();
        especialidad.setNombre(request.getNombre());
        especialidad.setDescripcion(request.getDescripcion());
        return toResponse(especialidadRepository.save(especialidad));
    }

    @Override
    public void eliminar(Long id) {
        if (!especialidadRepository.existsById(id)) {
            try {
                throw new ResourceNotFoundException("Especialidad no encontrada: " + id);
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        especialidadRepository.deleteById(id);
    }
}