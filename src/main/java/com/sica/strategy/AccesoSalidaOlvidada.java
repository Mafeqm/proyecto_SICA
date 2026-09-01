package com.sica.strategy;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Estrategia Concreta: AccesoSalidaOlvidada (Patrón Strategy).
 * Maneja la anomalía donde un visitante regresa a las instalaciones habiendo
 * omitido
 * el registro de su salida previa (la última visita continúa en estado
 * "EN_CURSO").
 * 
 * Regla de Negocio:
 * 1. Detecta la visita activa anterior.
 * 2. Realiza un cierre automático por sistema registrando la salida actual y
 * una observación de inconsistencia.
 * 3. Crea el nuevo registro de ingreso en estado "EN_CURSO".
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AccesoSalidaOlvidada implements EstrategiaAcceso {

    @Override
    public Visita procesarAcceso(Persona persona, VisitaDAO visitaDAO, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        System.out.println("[Estrategia AccesoSalidaOlvidada] Analizando historial de acceso para: "
                + persona.getNombreCompleto());

        // 1. Detectar visita previa que quedó en estado 'EN_CURSO'
        Optional<Visita> visitaActivaAnterior = visitaDAO.buscarUltimaVisitaActivaPorPersona(persona.getId());

        if (visitaActivaAnterior.isPresent() && "EN_CURSO".equalsIgnoreCase(visitaActivaAnterior.get().getEstado())) {
            Visita anterior = visitaActivaAnterior.get();
            System.out.println("[Estrategia AccesoSalidaOlvidada] ⚠️ Detectada visita anterior abierta (ID: "
                    + anterior.getId() + ")");

            String notaCierre = "[SISTEMA] Cierre automático forzado por salida no registrada al intentar nuevo ingreso.";
            boolean cerrado = visitaDAO.registrarSalida(anterior.getId(), LocalDateTime.now(), notaCierre);

            if (cerrado) {
                System.out.println("[Estrategia AccesoSalidaOlvidada] Visita ID " + anterior.getId()
                        + " cerrada automáticamente por el sistema.");
            }
        } else {
            System.out.println(
                    "[Estrategia AccesoSalidaOlvidada] No se encontraron visitas previas abiertas para la persona.");
        }

        // 2. Registrar la nueva visita de acceso
        Visita nuevaVisita = new Visita();
        nuevaVisita.setPersona(persona);
        nuevaVisita.setUsuarioRegistro(usuarioRegistro);
        nuevaVisita.setFechaEntrada(LocalDateTime.now());
        nuevaVisita.setMotivo(motivo != null ? motivo : "Reingreso por Salida Olvidada");
        nuevaVisita.setEstado("EN_CURSO");
        nuevaVisita.setObservaciones(
                "[ACCESO RE-INGRESO TRAS SALIDA OLVIDADA] " + (observaciones != null ? observaciones : ""));

        nuevaVisita = visitaDAO.crear(nuevaVisita);
        System.out
                .println("[Estrategia AccesoSalidaOlvidada] Nueva visita activa creada con ID: " + nuevaVisita.getId());

        return nuevaVisita;
    }

    @Override
    public String getNombreEstrategia() {
        return "ACCESO_SALIDA_OLVIDADA";
    }
}
