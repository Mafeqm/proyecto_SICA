package com.sica.strategy;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Estrategia Concreta: AccesoPreRegistrado (Patrón Strategy).
 * Maneja el flujo de ingreso de visitantes que ya cuentan con una cita o
 * autorización previa.
 * Valida la existencia de una reserva o genera directamente un ingreso activo
 * en estado "EN_CURSO".
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AccesoPreRegistrado implements EstrategiaAcceso {

    @Override
    public Visita procesarAcceso(Persona persona, VisitaDAO visitaDAO, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        System.out.println("[Estrategia AccesoPreRegistrado] Procesando ingreso pre-registrado para: "
                + persona.getNombreCompleto());

        // Verificar si existe una visita en estado PENDIENTE de autorización previa
        Optional<Visita> visitaExistente = visitaDAO.buscarUltimaVisitaActivaPorPersona(persona.getId());

        Visita visita;
        if (visitaExistente.isPresent() && "PENDIENTE".equalsIgnoreCase(visitaExistente.get().getEstado())) {
            visita = visitaExistente.get();
            visita.setFechaEntrada(LocalDateTime.now());
            visita.setEstado("EN_CURSO");
            visita.setObservaciones((visita.getObservaciones() != null ? visita.getObservaciones() + " | " : "")
                    + "[CHECK-IN PRE-REGISTRADO] " + (observaciones != null ? observaciones : ""));
            visitaDAO.actualizar(visita);
            System.out.println("[Estrategia AccesoPreRegistrado] Check-in completado para visita pre-existente ID: "
                    + visita.getId());
        } else {
            // Crear ingreso directo aprobado
            visita = new Visita();
            visita.setPersona(persona);
            visita.setUsuarioRegistro(usuarioRegistro);
            visita.setFechaEntrada(LocalDateTime.now());
            visita.setMotivo(motivo != null ? motivo : "Visita Pre-registrada / Cita Agendada");
            visita.setEstado("EN_CURSO");
            visita.setObservaciones("[ACCESO PRE-REGISTRADO] " + (observaciones != null ? observaciones : ""));
            visita = visitaDAO.crear(visita);
            System.out.println(
                    "[Estrategia AccesoPreRegistrado] Nueva visita registrada y activada con ID: " + visita.getId());
        }

        return visita;
    }

    @Override
    public String getNombreEstrategia() {
        return "ACCESO_PRE_REGISTRADO";
    }
}
