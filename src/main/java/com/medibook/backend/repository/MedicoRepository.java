package com.medibook.backend.repository;

import com.medibook.backend.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Buscar médicos por nombre de especialidad
    @Query("SELECT m FROM Medico m WHERE m.especialidad.nombre = :especialidad")
    List<Medico> findByEspecialidadNombre(@Param("especialidad") String especialidad);

    // Buscar médico por ID de usuario
    Optional<Medico> findByUsuarioId(Long usuarioId);
}
