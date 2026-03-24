package com.medibook.backend.controller;

import com.medibook.backend.dto.cita.CitaResponse;
import com.medibook.backend.dto.paciente.PacienteRequest;
import com.medibook.backend.dto.paciente.PacienteResponse;
import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.UsuarioRepository;
import com.medibook.backend.service.CitaService;
import com.medibook.backend.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;

    // Después del registro, el paciente completa su perfil médico
    @PostMapping("/perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> crearPerfil(
            @RequestBody PacienteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = resolverUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pacienteService.crearPerfil(usuarioId, request));
    }

    // El paciente consulta su propio perfil
    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> miPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = resolverUsuarioId(userDetails);
        return ResponseEntity.ok(pacienteService.obtenerPorUsuarioId(usuarioId));
    }

    // Actualizar perfil propio
    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> actualizarPerfil(
            @RequestBody PacienteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = resolverUsuarioId(userDetails);
        PacienteResponse paciente = pacienteService.obtenerPorUsuarioId(usuarioId);
        return ResponseEntity.ok(pacienteService.actualizar(paciente.getId(), request));
    }

    // El paciente consulta SUS propias citas
    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<CitaResponse>> misCitas(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = resolverUsuarioId(userDetails);
        PacienteResponse paciente = pacienteService.obtenerPorUsuarioId(usuarioId);
        return ResponseEntity.ok(citaService.obtenerCitasPorPaciente(paciente.getId()));
    }

    // Solo ADMIN puede ver la lista de todos los pacientes
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public ResponseEntity<List<PacienteResponse>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    // Helper privado: del JWT extraemos el email, y de ahí obtenemos el ID del usuario
    private Long resolverUsuarioId(UserDetails userDetails) {
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }

    // MEDICO o ADMIN pueden ver el historial completo de cualquier paciente
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN','PACIENTE')")
    public ResponseEntity<List<CitaResponse>> historialCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerHistorialCompletoPaciente(id));
    }
}