package com.medibook.backend.service.impl;

import com.medibook.backend.dto.estadisticas.EstadisticasResponse;
import com.medibook.backend.model.Cita;
import com.medibook.backend.model.EstadoCita;
import com.medibook.backend.repository.CitaRepository;
import com.medibook.backend.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements EstadisticasService {

    private final CitaRepository citaRepository;

    @Override
    public EstadisticasResponse obtenerEstadisticas() {

        // 1. Obtenemos TODAS las citas de la BD
        List<Cita> todas = citaRepository.findAll();
        long total = todas.size();

        // 2. Conteo por estado
        long pendientes  = contarPorEstado(todas, EstadoCita.PENDIENTE);
        long confirmadas = contarPorEstado(todas, EstadoCita.CONFIRMADA);
        long finalizadas = contarPorEstado(todas, EstadoCita.FINALIZADA);
        long canceladas  = contarPorEstado(todas, EstadoCita.CANCELADA);

        // 3. No-shows: citas PENDIENTE o CONFIRMADA cuya fechaHora ya pasó
        LocalDateTime ahora = LocalDateTime.now();
        List<Cita> noShowList = citaRepository.findNoShows(
                ahora,
                List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA)
        );
        long noShows = noShowList.size();

        // 4. Porcentaje de no-shows sobre el total
        // Evitamos división por cero con el operador ternario
        double porcentajeNoShow = total > 0
                ? Math.round((noShows * 100.0 / total) * 10.0) / 10.0
                : 0.0;

        // 5. Promedio de anticipación en días
        // fechaHora - fechaCreacion = días que el paciente agendó con anticipación
        double promedioAnticipacion = todas.stream()
                .filter(c -> c.getFechaCreacion() != null)
                .mapToLong(c -> java.time.temporal.ChronoUnit.DAYS.between(
                        c.getFechaCreacion(), c.getFechaHora()))
                .filter(dias -> dias >= 0) // ignoramos datos inconsistentes
                .average()
                .orElse(0.0);

        // Redondeamos a 1 decimal
        promedioAnticipacion = Math.round(promedioAnticipacion * 10.0) / 10.0;

        // 6. Distribución por hora del día (0-23)
        // Agrupamos todas las citas según la hora de su fechaHora
        Map<Integer, Long> citasPorHora = todas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFechaHora().getHour(),
                        Collectors.counting()
                ));

        // 7. Distribución por día de la semana
        // Usamos Locale("es") para obtener nombres en español: Lunes, Martes, etc.
        Map<String, Long> citasPorDiaSemana = todas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFechaHora()
                                .getDayOfWeek()
                                .getDisplayName(TextStyle.FULL, new Locale("es")),
                        Collectors.counting()
                ));

        // 8. Médicos con más citas finalizadas
        Map<String, Long> citasPorMedico = todas.stream()
                .filter(c -> c.getEstado() == EstadoCita.FINALIZADA)
                .collect(Collectors.groupingBy(
                        c -> c.getMedico().getUsuario().getNombre() + " "
                                + c.getMedico().getUsuario().getApellidos(),
                        Collectors.counting()
                ));

// 9. Especialidades más buscadas (por citas finalizadas)
        Map<String, Long> citasPorEspecialidad = todas.stream()
                .filter(c -> c.getEstado() == EstadoCita.FINALIZADA)
                .collect(Collectors.groupingBy(
                        c -> c.getMedico().getEspecialidad().getNombre(),
                        Collectors.counting()
                ));

        return EstadisticasResponse.builder()
                .totalCitas(total)
                .pendientes(pendientes)
                .confirmadas(confirmadas)
                .finalizadas(finalizadas)
                .canceladas(canceladas)
                .noShows(noShows)
                .porcentajeNoShow(porcentajeNoShow)
                .promedioAnticiapacionDias(promedioAnticipacion)
                .citasPorHora(citasPorHora)
                .citasPorDiaSemana(citasPorDiaSemana)
                .citasPorMedico(citasPorMedico)
                .citasPorEspecialidad(citasPorEspecialidad)
                .build();
    }

    // Helper privado para no repetir el filter+count
    private long contarPorEstado(List<Cita> citas, EstadoCita estado) {
        return citas.stream()
                .filter(c -> c.getEstado() == estado)
                .count();
    }


}