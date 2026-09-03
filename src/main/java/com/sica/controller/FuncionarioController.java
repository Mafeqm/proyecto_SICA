package com.sica.controller;

import com.sica.model.Visita;
import com.sica.service.VisitaService;

import java.util.List;

/**
 * Controlador para la pantalla de Funcionarios / Receptores de Visitas (Patrón
 * MVC).
 * 
 * Principios SOLID:
 * - Dependency Inversion Principle (DIP): Depende de VisitaService
 * (abstracción).
 * - Single Responsibility Principle (SRP): Orquestación de la vista de
 * funcionario y aprobación de accesos.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class FuncionarioController {

    private final VisitaService visitaServiceProxy;

    /**
     * Inyección de la abstracción del servicio de visitas.
     * 
     * @param visitaServiceProxy Servicio de visitas protegido.
     */
    public FuncionarioController(VisitaService visitaServiceProxy) {
        if (visitaServiceProxy == null) {
            throw new IllegalArgumentException("El controlador requiere una abstracción de VisitaService válida.");
        }
        this.visitaServiceProxy = visitaServiceProxy;
    }

    /**
     * Obtiene las visitas no anunciadas que están esperando aprobación.
     * 
     * @return Lista de visitas en estado PENDIENTE.
     */
    public List<Visita> listarVisitasPendientes() {
        return visitaServiceProxy.filtrarVisitasPorEstado(visitaServiceProxy.listarTodas(), "PENDIENTE");
    }

    /**
     * Aprobar una visita en estado PENDIENTE cambiando su estado a EN_CURSO.
     * 
     * @param visitaId      Identificador de la visita a aprobar.
     * @param observaciones Comentarios del funcionario al autorizar el ingreso.
     * @return true si la visita fue aprobada exitosamente.
     */
    public boolean aprobarVisita(Long visitaId, String observaciones) {
        List<Visita> todas = visitaServiceProxy.listarTodas();
        for (Visita v : todas) {
            if (v != null && v.getId() != null && v.getId().equals(visitaId)) {
                v.setEstado("EN_CURSO");
                v.setObservaciones((v.getObservaciones() != null ? v.getObservaciones() + " | " : "")
                        + "[APROBADO POR FUNCIONARIO] " + (observaciones != null ? observaciones : ""));
                return true;
            }
        }
        return false;
    }
}
