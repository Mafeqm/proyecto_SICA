package com.sica.strategy;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.observer.NotificadorVisitaSubject;

import java.time.LocalDateTime;

/**
 * Estrategia Concreta: AccesoNoAnunciado (Patrón Strategy + Integración con
 * Patrón Observer).
 * Maneja el flujo de visitantes que se presentan en recepción sin cita ni
 * autorización previa.
 * Genera un registro de visita en estado "PENDIENTE" de aprobación y gatilla la
 * notificación
 * a los funcionarios interesados mediante el Subject del patrón Observer.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AccesoNoAnunciado implements EstrategiaAcceso {

    private final NotificadorVisitaSubject notificadorSubject;

    /**
     * Constructor por defecto que utiliza la instancia global del Notificador.
     */
    public AccesoNoAnunciado() {
        this.notificadorSubject = NotificadorVisitaSubject.getInstanciaGlobal();
    }

    /**
     * Constructor inyectado para pruebas oSubjects personalizados.
     * 
     * @param notificadorSubject Instancia del sujeto observable.
     */
    public AccesoNoAnunciado(NotificadorVisitaSubject notificadorSubject) {
        this.notificadorSubject = notificadorSubject != null ? notificadorSubject
                : NotificadorVisitaSubject.getInstanciaGlobal();
    }

    @Override
    public Visita procesarAcceso(Persona persona, VisitaDAO visitaDAO, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        System.out.println(
                "[Estrategia AccesoNoAnunciado] Procesando visitante NO ANUNCIADO: " + persona.getNombreCompleto());

        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setUsuarioRegistro(usuarioRegistro);
        visita.setFechaEntrada(LocalDateTime.now());
        visita.setMotivo(motivo != null ? motivo : "Visita No Anunciada / En Espera de Autorización");
        visita.setEstado("PENDIENTE");
        visita.setObservaciones(
                "[ACCESO NO ANUNCIADO - REQUIERE APROBACIÓN] " + (observaciones != null ? observaciones : ""));

        // Persistir en base de datos
        visita = visitaDAO.crear(visita);
        System.out.println("[Estrategia AccesoNoAnunciado] Visita registrada exitosamente con ID: " + visita.getId()
                + " [Estado: PENDIENTE]");

        // Disparar evento del Patrón Observer
        if (notificadorSubject != null) {
            notificadorSubject.notificarObservadores(visita);
        }

        return visita;
    }

    @Override
    public String getNombreEstrategia() {
        return "ACCESO_NO_ANUNCIADO";
    }
}
