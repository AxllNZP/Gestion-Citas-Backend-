package com.medibook.backend.service;

import com.medibook.backend.model.Cita;

public interface EmailService {
    void enviarConfirmacionCita(Cita cita);
    void enviarCancelacionCita(Cita cita);
}