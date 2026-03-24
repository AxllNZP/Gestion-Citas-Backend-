package com.medibook.backend.dto.estadisticas;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class EstadisticasResponse {

    // ── Conteo por estado ─────────────────────────────────────
    private long totalCitas;
    private long pendientes;
    private long confirmadas;
    private long finalizadas;
    private long canceladas;

    // ── No-shows ──────────────────────────────────────────────
    // Citas PENDIENTE o CONFIRMADA cuya fechaHora ya pasó
    private long noShows;

    // Porcentaje de no-shows sobre el total de citas
    private double porcentajeNoShow;

    // ── Tiempos ───────────────────────────────────────────────
    // Promedio de días entre fechaCreacion y fechaHora
    private double promedioAnticiapacionDias;

    // Distribución por hora del día: clave = hora (0-23), valor = cantidad de citas
    private Map<Integer, Long> citasPorHora;

    // Distribución por día de la semana: clave = nombre del día, valor = cantidad
    private Map<String, Long> citasPorDiaSemana;

    // clave = nombre del médico, valor = cantidad finalizadas
    //MEDICO MAS BUSCADO
    private Map<String, Long> citasPorMedico;

    // clave = nombre especialidad, valor = cantidad finalizadas
    // ESPECIALIDAD MAS BUSCADA
    private Map<String, Long> citasPorEspecialidad;
}