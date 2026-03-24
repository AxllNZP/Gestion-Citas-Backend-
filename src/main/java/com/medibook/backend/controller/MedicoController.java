package com.medibook.backend.controller;

import com.medibook.backend.dto.cita.CitaResponse;
import com.medibook.backend.dto.medico.CrearMedicoCompletoRequest;
import com.medibook.backend.dto.medico.MedicoRequest;
import com.medibook.backend.dto.medico.MedicoResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;
    private final CitaService citaService;

    // GET público con filtro opcional por especialidad
    // ?especialidad=Cardiologia → filtra, sin parámetro → lista todos
    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listarTodos(
            @RequestParam(required = false) String especialidad) {
        if (especialidad != null && !especialidad.isBlank()) {
            return ResponseEntity.ok(medicoService.listarPorEspecialidad(especialidad));
        }
        return ResponseEntity.ok(medicoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.obtenerPorId(id));
    }

    // Solo ADMIN puede registrar médicos
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.crear(request));
    }

    // Un médico ve SUS propias citas
    // @AuthenticationPrincipal inyecta el UserDetails del token JWT actual
    // → No necesitamos que el médico envíe su ID; lo sacamos del token (seguro)
    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<CitaResponse>> misCitas(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Del token obtenemos email → buscamos médico → buscamos sus citas
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(citaService.obtenerCitasPorMedico(medico.getId()));
    }

    // El médico consulta su propio perfil
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MedicoResponse> miPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(medicoService.obtenerPorEmail(userDetails.getUsername()));
    }

    // El médico actualiza su teléfono
    @PatchMapping("/mi-perfil/telefono")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MedicoResponse> actualizarTelefono(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody java.util.Map<String, String> body) {
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(medicoService.actualizarTelefono(medico.getId(), body.get("telefono")));
    }

    @PostMapping("/crear-completo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicoResponse> crearCompleto(
            @Valid @RequestBody CrearMedicoCompletoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicoService.crearCompleto(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.ok(medicoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // El médico actualiza SU PROPIO perfil — el email viene del JWT, no del body
    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MedicoResponse> actualizarMiPerfil(
            @RequestBody MedicoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(medicoService.actualizarPorEmail(userDetails.getUsername(), request));
    }


    // El médico ve los pacientes que ha atendido (citas FINALIZADAS)
    @GetMapping("/mis-pacientes")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<com.medibook.backend.dto.paciente.PacienteResponse>> misPacientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(citaService.obtenerPacientesAtendidosPorMedico(medico.getId()));
    }

    // El médico ve el historial completo de un paciente específico
    @GetMapping("/historial/{pacienteId}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<CitaResponse>> historialPaciente(
            @PathVariable Long pacienteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        MedicoResponse medico = medicoService.obtenerPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(citaService.obtenerHistorialPaciente(medico.getId(), pacienteId));
    }
}