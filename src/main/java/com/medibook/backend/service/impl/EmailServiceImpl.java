package com.medibook.backend.service.impl;

import com.medibook.backend.model.Cita;
import com.medibook.backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;

// En vez de SMTP (bloqueado por la red), usamos la API HTTP de Mailtrap.
// La API usa puerto 443 (HTTPS) que nunca está bloqueado.
// Enviamos un JSON con los datos del email directamente a su endpoint REST.
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${mailtrap.api.token}")
    private String apiToken;

    @Value("${mailtrap.inbox.id}")
    private String inboxId;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void enviarConfirmacionCita(Cita cita) {
        String asunto = "Confirmacion de Cita - MediBook";
        String cuerpo = "Hola " + cita.getPaciente().getUsuario().getNombre() + ",\n\n"
                + "Su cita ha sido agendada exitosamente.\n\n"
                + "Fecha y Hora: " + cita.getFechaHora().format(FORMATTER) + "\n"
                + "Medico: Dr/Dra. " + cita.getMedico().getUsuario().getNombre() + " "
                + cita.getMedico().getUsuario().getApellidos() + "\n"
                + "Especialidad: " + cita.getMedico().getEspecialidad().getNombre() + "\n"
                + "Motivo: " + (cita.getMotivo() != null ? cita.getMotivo() : "No especificado") + "\n\n"
                + "Recuerde llegar 10 minutos antes.\n\nEquipo MediBook";

        enviar(cita.getPaciente().getUsuario().getEmail(),
                cita.getPaciente().getUsuario().getNombre(), asunto, cuerpo);
    }

    @Override
    public void enviarCancelacionCita(Cita cita) {
        String asunto = "Cita Cancelada - MediBook";
        String cuerpo = "Hola " + cita.getPaciente().getUsuario().getNombre() + ",\n\n"
                + "Su cita del " + cita.getFechaHora().format(FORMATTER) + " ha sido cancelada.\n\n"
                + "Si desea reagendar, ingrese a MediBook.\n\nEquipo MediBook";

        enviar(cita.getPaciente().getUsuario().getEmail(),
                cita.getPaciente().getUsuario().getNombre(), asunto, cuerpo);
    }

    private void enviar(String destinatarioEmail, String destinatarioNombre,
                        String asunto, String cuerpo) {
        try {
            // Construimos el JSON del email manualmente (sin librería externa)
            String json = """
                    {
                        "from": { "email": "noreply@medibook.com", "name": "MediBook" },
                        "to": [{ "email": "%s", "name": "%s" }],
                        "subject": "%s",
                        "text": "%s"
                    }
                    """.formatted(
                    destinatarioEmail,
                    destinatarioNombre,
                    asunto,
                    cuerpo.replace("\n", "\\n").replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://sandbox.api.mailtrap.io/api/send/" + inboxId))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("Email enviado a: {}", destinatarioEmail);
            } else {
                log.error("Error Mailtrap API: status={} body={}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Error enviando email: {}", e.getMessage());
        }
    }
}