package com.sica.controller;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.VisitaService;
import com.sica.strategy.EstrategiaAcceso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para el Módulo de Guardias de Seguridad (Patrón MVC).
 * Interactúa con la capa de servicio a través de la abstracción VisitaService
 * (Inversión de Dependencias - DIP),
 * garantizando que el Proxy de Seguridad RBAC intercepte y valide los permisos
 * necesarios.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class GuardaController {

    private final VisitaService visitaServiceProxy;

    /**
     * Inyección de la abstracción del servicio de visitas (generalmente envuelto
     * por el Proxy RBAC).
     * 
     * @param visitaServiceProxy Servicio de visitas (abstracción).
     */
    public GuardaController(VisitaService visitaServiceProxy) {
        if (visitaServiceProxy == null) {
            throw new IllegalArgumentException("El controlador requiere una abstracción de VisitaService válida.");
        }
        this.visitaServiceProxy = visitaServiceProxy;
    }

    /**
     * Procesa la solicitud de ingreso de una persona.
     * 
     * @param persona        Persona que ingresa.
     * @param estrategia     Algoritmo de flujo de acceso seleccionada.
     * @param usuarioGuardia Guardia responsable.
     * @param motivo         Motivo de la visita.
     * @param observaciones  Comentarios adicionales.
     * @return Visita generada.
     * @throws AccesoDenegadoException Si el usuario no tiene el permiso RBAC
     *                                 'VISITA_REGISTRAR'.
     */
    public Visita registrarIngreso(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioGuardia,
            String motivo, String observaciones) throws AccesoDenegadoException {
        if (visitaServiceProxy instanceof SeguridadVisitaServiceProxy) {
            return ((SeguridadVisitaServiceProxy) visitaServiceProxy)
                    .registrarAccesoConExcepcion(persona, estrategia, usuarioGuardia, motivo, observaciones);
        }
        return visitaServiceProxy.registrarAcceso(persona, estrategia, usuarioGuardia, motivo, observaciones);
    }

    /**
     * Procesa el registro de salida de un visitante.
     * 
     * @param visitaId      Identificador de la visita.
     * @param observaciones Observaciones de salida.
     * @return true si la salida fue registrada.
     * @throws AccesoDenegadoException Si el usuario carece del permiso RBAC
     *                                 'VISITA_SALIDA'.
     */
    public boolean registrarSalida(Long visitaId, String observaciones) throws AccesoDenegadoException {
        if (visitaServiceProxy instanceof SeguridadVisitaServiceProxy) {
            return ((SeguridadVisitaServiceProxy) visitaServiceProxy)
                    .registrarSalidaConExcepcion(visitaId, LocalDateTime.now(), observaciones);
        }
        return visitaServiceProxy.registrarSalida(visitaId, LocalDateTime.now(), observaciones);
    }

    /**
     * Consulta la lista de visitas que están actualmente en curso.
     * 
     * @return Lista de visitas activas.
     */
    public List<Visita> listarVisitasEnCurso() {
        return visitaServiceProxy.filtrarVisitasPorEstado(visitaServiceProxy.listarTodas(), "EN_CURSO");
    }
}
