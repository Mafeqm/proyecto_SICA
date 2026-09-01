package com.sica.service.impl;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.service.VisitaService;
import com.sica.strategy.EstrategiaAcceso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementación base de la Capa de Servicio para la gestión de Visitas.
 * Aplica el patrón Strategy para la ejecución de los algoritmos de ingreso
 * y utiliza de manera intensiva expresiones Lambda y la API Stream para
 * operaciones
 * declarativas sobre colecciones de datos.
 * 
 * Principios SOLID:
 * - Single Responsibility Principle (SRP): Lógica de orquestación de servicios
 * de visitas.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class VisitaServiceImpl implements VisitaService {

    private final VisitaDAO visitaDAO;

    public VisitaServiceImpl(VisitaDAO visitaDAO) {
        this.visitaDAO = visitaDAO;
    }

    @Override
    public Visita registrarAcceso(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona requerida para el registro de acceso no puede ser nula.");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("Debe proveerse una instancia de EstrategiaAcceso válida.");
        }

        // Delegación de la lógica del flujo de acceso al patrón Strategy
        return estrategia.procesarAcceso(persona, visitaDAO, usuarioRegistro, motivo, observaciones);
    }

    @Override
    public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
        if (visitaId == null)
            return false;
        return visitaDAO.registrarSalida(visitaId, fechaSalida != null ? fechaSalida : LocalDateTime.now(),
                observaciones);
    }

    @Override
    public List<Visita> listarTodas() {
        return visitaDAO.listarTodos();
    }

    /**
     * Filtra visitas según el estado provisto utilizando Java Stream API y
     * Expresiones Lambda.
     */
    @Override
    public List<Visita> filtrarVisitasPorEstado(List<Visita> visitas, String estado) {
        if (visitas == null || estado == null)
            return new ArrayList<>();

        return visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getEstado() != null && estado.equalsIgnoreCase(v.getEstado().trim()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de visitas pertenecientes a una persona específica
     * utilizando Stream API.
     */
    @Override
    public List<Visita> obtenerVisitasRecientesPorPersona(List<Visita> visitas, Long personaId) {
        if (visitas == null || personaId == null)
            return new ArrayList<>();

        return visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getPersona() != null && personaId.equals(v.getPersona().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Cuenta la cantidad de visitas activas ('EN_CURSO') utilizando Stream API.
     */
    @Override
    public long contarVisitasActivas(List<Visita> visitas) {
        if (visitas == null)
            return 0;

        return visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> "EN_CURSO".equalsIgnoreCase(v.getEstado()))
                .count();
    }
}
