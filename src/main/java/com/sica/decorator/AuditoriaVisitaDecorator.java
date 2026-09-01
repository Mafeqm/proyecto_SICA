package com.sica.decorator;

import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.model.BitacoraAuditoria;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.service.VisitaService;
import com.sica.strategy.EstrategiaAcceso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Decorador Concreto (Patrón de Diseño Decorator).
 * Envuelve una instancia de VisitaService para añadir una capa transparente de
 * Auditoría.
 * Intercepta los eventos exitosos de registro de acceso y salida, insertando
 * automáticamente
 * un registro en la bitácora de auditoría sin alterar ni acoplar la lógica de
 * negocio original.
 * 
 * Principios SOLID:
 * - Single Responsibility Principle (SRP): Su única responsabilidad es auditar
 * ejecuciones del servicio.
 * - Open/Closed Principle (OCP): Añade comportamiento dinámicamente sin
 * modificar VisitaServiceImpl.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AuditoriaVisitaDecorator implements VisitaService {

    private final VisitaService servicioDecorado;
    private final BitacoraAuditoriaDAO bitacoraDAO;

    /**
     * Constructor que recibe el servicio objetivo y el DAO de auditoría.
     * 
     * @param servicioDecorado Instancia base de VisitaService a envolver.
     * @param bitacoraDAO      DAO para guardar los registros de auditoría.
     */
    public AuditoriaVisitaDecorator(VisitaService servicioDecorado, BitacoraAuditoriaDAO bitacoraDAO) {
        this.servicioDecorado = servicioDecorado;
        this.bitacoraDAO = bitacoraDAO;
    }

    @Override
    public Visita registrarAcceso(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        // 1. Ejecutar la lógica de negocio del servicio envuelto
        Visita visitaProcesada = servicioDecorado.registrarAcceso(persona, estrategia, usuarioRegistro, motivo,
                observaciones);

        // 2. Interceptar y auditar automáticamente si fue exitoso
        if (visitaProcesada != null && visitaProcesada.getId() != null) {
            String accionStr = "REGISTRO_ACCESO_"
                    + (estrategia != null ? estrategia.getNombreEstrategia() : "DESCONOCIDO");
            String detalleStr = "Ingreso de persona: " + persona.getNombreCompleto() + " (Doc: "
                    + persona.getNumeroDocumento()
                    + ") | Estado: " + visitaProcesada.getEstado() + " | Motivo: " + motivo;

            BitacoraAuditoria log = new BitacoraAuditoria(
                    usuarioRegistro,
                    accionStr,
                    "visita",
                    visitaProcesada.getId(),
                    detalleStr,
                    "127.0.0.1");

            if (bitacoraDAO != null) {
                bitacoraDAO.registrar(log);
                System.out.println(
                        "[DECORATOR AUDITORÍA] 📝 Evento de ingreso registrado en Bitácora de Auditoría (Log ID asignado).");
            }
        }

        return visitaProcesada;
    }

    @Override
    public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
        // 1. Ejecutar la operación original
        boolean exito = servicioDecorado.registrarSalida(visitaId, fechaSalida, observaciones);

        // 2. Interceptar y auditar automáticamente
        if (exito) {
            String detalleStr = "Egreso registrado para visita ID: " + visitaId + " | Obs: " + observaciones;

            BitacoraAuditoria log = new BitacoraAuditoria(
                    null, // Sistema / Usuario automático
                    "REGISTRO_SALIDA",
                    "visita",
                    visitaId,
                    detalleStr,
                    "127.0.0.1");

            if (bitacoraDAO != null) {
                bitacoraDAO.registrar(log);
                System.out.println("[DECORATOR AUDITORÍA] 📝 Evento de salida registrado en Bitácora de Auditoría.");
            }
        }

        return exito;
    }

    // Delegación directa de métodos de consulta sin decoración

    @Override
    public List<Visita> listarTodas() {
        return servicioDecorado.listarTodas();
    }

    @Override
    public List<Visita> filtrarVisitasPorEstado(List<Visita> visitas, String estado) {
        return servicioDecorado.filtrarVisitasPorEstado(visitas, estado);
    }

    @Override
    public List<Visita> obtenerVisitasRecientesPorPersona(List<Visita> visitas, Long personaId) {
        return servicioDecorado.obtenerVisitasRecientesPorPersona(visitas, personaId);
    }

    @Override
    public long contarVisitasActivas(List<Visita> visitas) {
        return servicioDecorado.contarVisitasActivas(visitas);
    }
}
