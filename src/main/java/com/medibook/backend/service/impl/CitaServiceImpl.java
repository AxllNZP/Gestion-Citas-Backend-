package com.medibook.backend.service.impl;

import com.medibook.backend.dto.cita.ActualizarEstadoCitaRequest;
import com.medibook.backend.dto.cita.CitaRequest;
import com.medibook.backend.dto.cita.CitaResponse;
import com.medibook.backend.dto.cita.FinalizarCitaRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;
import com.medibook.backend.exception.ConflictException;
import com.medibook.backend.exception.ResourceNotFoundException;
import com.medibook.backend.model.*;
import com.medibook.backend.repository.*;
import com.medibook.backend.service.CitaService;
import com.medibook.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    // Reemplaza el método toResponse() existente con este:
    private CitaResponse toResponse(Cita c) {
        return CitaResponse.builder()
                .id(c.getId())
                .fechaHora(c.getFechaHora())
                .motivo(c.getMotivo())
                .estado(c.getEstado())
                .diagnostico(c.getDiagnostico())     // ← DIAGNOSTICO
                .indicaciones(c.getIndicaciones())   // ← INDICACIONES
                .pacienteId(c.getPaciente().getId())
                .pacienteNombre(c.getPaciente().getUsuario().getNombre() + " "
                        + c.getPaciente().getUsuario().getApellidos())
                .medicoId(c.getMedico().getId())
                .medicoNombre(c.getMedico().getUsuario().getNombre() + " "
                        + c.getMedico().getUsuario().getApellidos())
                .especialidad(c.getMedico().getEspecialidad().getNombre())
                .build();
    }

    @Override
    public CitaResponse crearCita(Long pacienteUsuarioId, CitaRequest request) {
        // Buscamos el perfil Paciente del usuario autenticado
        // Si el usuario no completó su perfil, lanzamos error descriptivo
        Paciente paciente = pacienteRepository.findByUsuarioId(pacienteUsuarioId)
                .orElseThrow();

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow();

        // *** LÓGICA DE NEGOCIO CLAVE: Validación de conflictos de horario ***
        // Verificamos que el médico NO tenga otra cita en exactamente ese mismo horario.
        // Esto es lo que pedía el desafío: "validar que no se solapen citas".
        List<Cita> conflictos = citaRepository.findCitasByMedicoAndFecha(
                medico.getId(), request.getFechaHora()
        );

        if (!conflictos.isEmpty()) {
            throw new ConflictException("El médico ya tiene una cita agendada en ese horario");
        }

        Cita cita = Cita.builder()
                .fechaHora(request.getFechaHora())
                .fechaCreacion(LocalDateTime.now())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .paciente(paciente)
                .medico(medico)
                .build();

        Cita citaGuardada = citaRepository.save(cita);

        // Enviamos email de confirmación (si falla el email, la cita YA está guardada)
        emailService.enviarConfirmacionCita(citaGuardada);

        return toResponse(citaGuardada);
    }

    @Override
    public List<CitaResponse> obtenerCitasPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CitaResponse> obtenerCitasPorMedico(Long medicoId) {
        return citaRepository.findByMedicoId(medicoId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CitaResponse> listarTodas() {
        return citaRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public CitaResponse actualizarEstado(Long citaId, ActualizarEstadoCitaRequest request) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow();
        cita.setEstado(request.getEstado());
        return toResponse(citaRepository.save(cita));
    }

    @Override
    public void cancelarCita(Long citaId, String emailUsuario) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow();

        // *** SEGURIDAD: Solo el dueño de la cita o un Admin puede cancelarla ***
        // Un paciente NO puede cancelar citas de otro paciente
        boolean esPacienteDeLaCita = cita.getPaciente().getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN"));

        if (!esPacienteDeLaCita && !esAdmin) {
            throw new AccessDeniedException("No tiene permiso para cancelar esta cita");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        Cita citaCancelada = citaRepository.save(cita);

        emailService.enviarCancelacionCita(citaCancelada);
    }

    // Agregar después del método actualizarEstado():

    @Override
    public CitaResponse finalizarCita(Long citaId, FinalizarCitaRequest request) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + citaId));

        // Solo se puede finalizar una cita CONFIRMADA
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new RuntimeException("Solo se pueden finalizar citas CONFIRMADAS");
        }

        cita.setEstado(EstadoCita.FINALIZADA);
        cita.setDiagnostico(request.getDiagnostico());
        cita.setIndicaciones(request.getIndicaciones());

        return toResponse(citaRepository.save(cita));
    }



    //Pacientes atendidos
    @Override
    public List<PacienteResponse> obtenerPacientesAtendidosPorMedico(Long medicoId) {
        // Solo devuelve pacientes de citas FINALIZADAS con este médico
        return citaRepository.findPacientesAtendidosByMedico(medicoId, EstadoCita.FINALIZADA)
                .stream()
                .map(p -> PacienteResponse.builder()
                        .id(p.getId())
                        .telefono(p.getTelefono())
                        .direccion(p.getDireccion())
                        .fechaNacimiento(p.getFechaNacimiento())
                        .grupoSanguineo(p.getGrupoSanguineo())
                        .usuarioId(p.getUsuario().getId())
                        .nombre(p.getUsuario().getNombre())
                        .apellidos(p.getUsuario().getApellidos())
                        .email(p.getUsuario().getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    //HISTORIAL DE PACIENTE
    @Override
    public List<CitaResponse> obtenerHistorialPaciente(Long medicoId, Long pacienteId) {
        // Seguridad: filtramos por medicoId + pacienteId + FINALIZADA
        // Un médico NUNCA puede ver citas de otro médico con este endpoint
        return citaRepository.findByMedicoIdAndPacienteIdAndEstadoOrderByFechaHoraDesc(
                        medicoId, pacienteId, EstadoCita.FINALIZADA)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaResponse> obtenerHistorialCompletoPaciente(Long pacienteId) {
        return citaRepository
                .findByPacienteIdAndEstadoOrderByFechaHoraDesc(pacienteId, EstadoCita.FINALIZADA)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}