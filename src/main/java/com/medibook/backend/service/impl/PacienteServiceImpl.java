package com.medibook.backend.service.impl;

import com.medibook.backend.dto.paciente.PacienteRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;
import com.medibook.backend.exception.ResourceNotFoundException;
import com.medibook.backend.model.Paciente;
import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.PacienteRepository;
import com.medibook.backend.repository.UsuarioRepository;
import com.medibook.backend.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    private PacienteResponse toResponse(Paciente p) {
        return PacienteResponse.builder()
                .id(p.getId())
                .telefono(p.getTelefono())
                .direccion(p.getDireccion())
                .fechaNacimiento(p.getFechaNacimiento())
                .grupoSanguineo(p.getGrupoSanguineo())
                .usuarioId(p.getUsuario().getId())
                .nombre(p.getUsuario().getNombre())
                .apellidos(p.getUsuario().getApellidos())
                .email(p.getUsuario().getEmail())
                .build();
    }

    @Override
    public PacienteResponse crearPerfil(Long usuarioId, PacienteRequest request) {
        Usuario usuario = null;
        try {
            usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }

        Paciente paciente = Paciente.builder()
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .fechaNacimiento(request.getFechaNacimiento())
                .grupoSanguineo(request.getGrupoSanguineo())
                .usuario(usuario)
                .build();

        return toResponse(pacienteRepository.save(paciente));
    }

    @Override
    public PacienteResponse obtenerPorId(Long id) {
        try {
            return toResponse(pacienteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id)));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PacienteResponse obtenerPorUsuarioId(Long usuarioId) {
        try {
            return toResponse(pacienteRepository.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado para usuario: " + usuarioId)));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PacienteResponse> listarTodos() {
        return pacienteRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = null;
        try {
            paciente = pacienteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        paciente.setTelefono(request.getTelefono());
        paciente.setDireccion(request.getDireccion());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setGrupoSanguineo(request.getGrupoSanguineo());
        return toResponse(pacienteRepository.save(paciente));
    }
}