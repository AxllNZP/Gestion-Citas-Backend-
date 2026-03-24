package com.medibook.backend.controller;

import com.medibook.backend.dto.cita.ActualizarEstadoCitaRequest;
import com.medibook.backend.dto.cita.CitaRequest;
import com.medibook.backend.dto.cita.CitaResponse;
import com.medibook.backend.dto.cita.FinalizarCitaRequest;
import com.medibook.backend.dto.medico.MedicoResponse;
import com.medibook.backend.dto.paciente.PacienteResponse;
import com.medibook.backend.repository.UsuarioRepository;
import com.medibook.backend.service.CitaService;
import com.medibook.backend.service.MedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.medibook.backend.dto.cita.FinalizarCitaRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;
    private final MedicoService medicoService;


    // PACIENTE agenda una nueva cita
    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponse> crearCita(
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = resolverUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citaService.crearCita(usuarioId, request));
    }

    // ADMIN ve todas las citas del sistema
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CitaResponse>> listarTodas() {
        return ResponseEntity.ok(citaService.listarTodas());
    }

    // MÉDICO o ADMIN cambia el estado (confirmar, finalizar)
    // PATCH es más semántico que PUT para actualizaciones parciales
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    public ResponseEntity<CitaResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoCitaRequest request) {
        return ResponseEntity.ok(citaService.actualizarEstado(id, request));
    }

    // PACIENTE o ADMIN cancela una cita
    // La lógica de seguridad (que el paciente solo cancele SUS citas) está en el Service
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
    public ResponseEntity<Void> cancelarCita(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        citaService.cancelarCita(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    private Long resolverUsuarioId(UserDetails userDetails) {
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }

    // MÉDICO finaliza la cita con diagnóstico e indicaciones
    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    public ResponseEntity<CitaResponse> finalizarCita(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarCitaRequest request) {
        return ResponseEntity.ok(citaService.finalizarCita(id, request));
    }

    // Devuelve la lista de pacientes atendidos por este médico (solo FINALIZADAS)
    @GetMapping("/mis-pacientes")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<PacienteResponse>> misPacientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(citaService.obtenerPacientesAtendidosPorMedico(medico.getId()));
    }

    // Devuelve el historial clínico de UN paciente atendido por este médico
    @GetMapping("/historial/{pacienteId}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<CitaResponse>> historialPaciente(
            @PathVariable Long pacienteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(citaService.obtenerHistorialPaciente(medico.getId(), pacienteId));
    }

}