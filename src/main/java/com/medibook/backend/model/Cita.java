package com.medibook.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    // Fecha en que el paciente agendó la cita
    // @Column con updatable=false → nunca se modifica después de crearse
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    // APLICACION DE DIAGNOSTICO Y INDICACIONES

    @Column(columnDefinition = "TEXT")
    private String diagnostico;   // Diagnóstico / qué medicación se receta

    @Column(columnDefinition = "TEXT")
    private String indicaciones;  // Cómo tomar las medicaciones
}
