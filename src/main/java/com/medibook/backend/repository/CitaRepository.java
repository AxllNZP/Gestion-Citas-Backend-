package com.medibook.backend.repository;

import com.medibook.backend.model.Cita;
import com.medibook.backend.model.EstadoCita;
import com.medibook.backend.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByMedicoId(Long medicoId);

    // Verificar si un médico tiene una cita en ese rango de tiempo (para evitar conflictos)
    @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId AND c.fechaHora = :fechaHora")
    List<Cita> findCitasByMedicoAndFecha(@Param("medicoId") Long medicoId, @Param("fechaHora") LocalDateTime fechaHora);



    /// /////////////////////////////////////////////////////////////////////////////////
    ///
    ///

    // Devuelve pacientes ÚNICOS que este médico ha atendido (solo citas FINALIZADAS)
    // DISTINCT evita duplicados si un paciente tuvo varias citas con el mismo médico
    @Query("SELECT DISTINCT c.paciente FROM Cita c WHERE c.medico.id = :medicoId AND c.estado = :estado")
    List<Paciente> findPacientesAtendidosByMedico(
            @Param("medicoId") Long medicoId,
            @Param("estado") EstadoCita estado
    );

    // Devuelve el historial completo de un paciente CON este médico específico
    // OrderBy fecha descendente → la más reciente aparece primero
    List<Cita> findByMedicoIdAndPacienteIdAndEstadoOrderByFechaHoraDesc(
            Long medicoId, Long pacienteId, EstadoCita estado);

    //ORDENAR LAS CITAS DEL PACIENTE
    List<Cita> findByPacienteIdAndEstadoOrderByFechaHoraDesc(Long pacienteId, EstadoCita estado);


    // ── Nuevo query para no-shows ─────────────────────────────────────────────
    // Busca citas cuya fechaHora ya pasó Y su estado sigue siendo PENDIENTE o CONFIRMADA
    // Eso significa que el paciente no se presentó y nadie actualizó el estado
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fechaHora < :ahora
            AND c.estado IN :estados
            """)
    List<Cita> findNoShows(
            @Param("ahora") LocalDateTime ahora,
            @Param("estados") List<EstadoCita> estados);
}
