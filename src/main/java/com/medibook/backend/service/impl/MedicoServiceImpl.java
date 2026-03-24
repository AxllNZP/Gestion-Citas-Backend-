package com.medibook.backend.service.impl;

import com.medibook.backend.dto.medico.CrearMedicoCompletoRequest;
import com.medibook.backend.dto.medico.MedicoRequest;
import com.medibook.backend.dto.medico.MedicoResponse;
import com.medibook.backend.exception.ResourceNotFoundException;
import com.medibook.backend.model.Especialidad;
import com.medibook.backend.model.Medico;
import com.medibook.backend.model.Rol;
import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.EspecialidadRepository;
import com.medibook.backend.repository.MedicoRepository;
import com.medibook.backend.repository.RolRepository;
import com.medibook.backend.repository.UsuarioRepository;
import com.medibook.backend.service.MedicoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    private MedicoResponse toResponse(Medico m) {
        return MedicoResponse.builder()
                .id(m.getId())
                .cmp(m.getCmp())
                .telefono(m.getTelefono())
                .usuarioId(m.getUsuario().getId())
                .nombre(m.getUsuario().getNombre())
                .apellidos(m.getUsuario().getApellidos())
                .email(m.getUsuario().getEmail())
                .especialidadId(m.getEspecialidad().getId())
                .especialidadNombre(m.getEspecialidad().getNombre())
                .build();
    }

    @Override
    public MedicoResponse crear(MedicoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow();
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow();

        // Asignar ROLE_MEDICO al usuario
        Rol rolMedico = rolRepository.findByNombre("ROLE_MEDICO").orElseThrow();
        usuario.getRoles().add(rolMedico);
        usuarioRepository.save(usuario);

        Medico medico = Medico.builder()
                .cmp(request.getCmp())
                .telefono(request.getTelefono())
                .usuario(usuario)
                .especialidad(especialidad)
                .build();

        return toResponse(medicoRepository.save(medico));
    }

    @Override
    public List<MedicoResponse> listarTodos() {
        return medicoRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<MedicoResponse> listarPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidadNombre(especialidad).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public MedicoResponse obtenerPorId(Long id) {
        return toResponse(medicoRepository.findById(id)
                .orElseThrow());
    }

    @Override
    public MedicoResponse actualizarTelefono(Long id, String telefono) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        medico.setTelefono(telefono);
        return toResponse(medicoRepository.save(medico));
    }

    @Override
    @Transactional
    public MedicoResponse crearCompleto(CrearMedicoCompletoRequest request) {

        // 1. Verificar email no duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        // 2. Crear el usuario
        Rol rolMedico = rolRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new RuntimeException("Rol MEDICO no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.getRoles().add(rolMedico);
        usuario = usuarioRepository.save(usuario);

        // 3. Crear el perfil médico
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setCmp(request.getCmp());
        medico.setTelefono(request.getTelefono());
        medico.setEspecialidad(especialidad);

        return toResponse(medicoRepository.save(medico));
    }

    @Override
    public MedicoResponse actualizar(Long id, MedicoRequest request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        medico.setCmp(request.getCmp());
        medico.setTelefono(request.getTelefono());
        medico.setEspecialidad(especialidad);

        return toResponse(medicoRepository.save(medico));
    }

    @Override
    public void eliminar(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new RuntimeException("Médico no encontrado");
        }
        medicoRepository.deleteById(id);
    }

    @Override
    public MedicoResponse obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow();
        return toResponse(medicoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow());
    }

    @Override
    public MedicoResponse actualizarPorEmail(String email, MedicoRequest request) {
        // 1. Encontramos al médico usando el email del JWT (el médico no puede editar a otro)
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Medico medico = medicoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de médico no encontrado"));

        // 2. Si cambió la especialidad, la buscamos
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        // 3. Actualizamos solo los campos editables
        medico.setCmp(request.getCmp());
        medico.setTelefono(request.getTelefono());
        medico.setEspecialidad(especialidad);

        return toResponse(medicoRepository.save(medico));
    }
}