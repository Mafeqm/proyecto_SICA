package com.sica.controller;

import com.sica.model.Visita;
import com.sica.service.VisitaService;

import java.util.List;
import java.util.Objects;

/**
 * Controlador para el Módulo de Supervisión y Reportes (Patrón MVC).
 * Implementa el procesamiento declarativo de métricas de accesos y visitas
 * utilizando estrictamente la API Stream de Java 8+ y Expresiones Lambda.
 * 
 * Principios SOLID:
 * - Single Responsibility Principle (SRP): Orquestación y cálculo de métricas
 * de supervisión.
 * - Dependency Inversion Principle (DIP): Depende de la interfaz VisitaService.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class SupervisorController {

    private final VisitaService visitaServiceProxy;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param visitaServiceProxy Servicio de visitas (abstracción protegida por
     *                           Proxy).
     */
    public SupervisorController(VisitaService visitaServiceProxy) {
        if (visitaServiceProxy == null) {
            throw new IllegalArgumentException("El controlador requiere una abstracción de VisitaService válida.");
        }
        this.visitaServiceProxy = visitaServiceProxy;
    }

    /**
     * Obtiene y calcula el reporte métrico de personas en el complejo utilizando
     * expresiones Lambda y la API Stream.
     * 
     * @return DTO con contadores procesados por Stream API.
     */
    public ReporteMetricasVisitas obtenerReporteConsolidado() {
        List<Visita> todas = visitaServiceProxy.listarTodas();
        return calcularMetricasStream(todas);
    }

    /**
     * Genera un reporte métrico de personas en el complejo utilizando expresiones
     * Lambda y la API Stream.
     * 
     * @param visitas Lista de visitas a analizar.
     * @return DTO con contadores procesados por Stream API.
     */
    public ReporteMetricasVisitas calcularMetricasStream(List<Visita> visitas) {
        if (visitas == null) {
            return new ReporteMetricasVisitas(0, 0, 0, 0);
        }

        // USO ESTRICTO DE STREAM API Y LAMBDAS
        long totalRegistros = visitas.stream()
                .filter(Objects::nonNull)
                .count();

        long dentroEnCurso = visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> "EN_CURSO".equalsIgnoreCase(v.getEstado()))
                .count();

        long pendientesAprobacion = visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> "PENDIENTE".equalsIgnoreCase(v.getEstado()))
                .count();

        long salidasFinalizadas = visitas.stream()
                .filter(Objects::nonNull)
                .filter(v -> "FINALIZADA".equalsIgnoreCase(v.getEstado()))
                .count();

        return new ReporteMetricasVisitas(totalRegistros, dentroEnCurso, pendientesAprobacion, salidasFinalizadas);
    }

    /**
     * Clase de Datos Interna para las Métricas de Supervisión.
     */
    public static class ReporteMetricasVisitas {
        private final long totalRegistros;
        private final long dentroEnCurso;
        private final long pendientesAprobacion;
        private final long salidasFinalizadas;

        public ReporteMetricasVisitas(long totalRegistros, long dentroEnCurso, long pendientesAprobacion,
                long salidasFinalizadas) {
            this.totalRegistros = totalRegistros;
            this.dentroEnCurso = dentroEnCurso;
            this.pendientesAprobacion = pendientesAprobacion;
            this.salidasFinalizadas = salidasFinalizadas;
        }

        public long getTotalRegistros() {
            return totalRegistros;
        }

        public long getDentroEnCurso() {
            return dentroEnCurso;
        }

        public long getPendientesAprobacion() {
            return pendientesAprobacion;
        }

        public long getSalidasFinalizadas() {
            return salidasFinalizadas;
        }

        @Override
        public String toString() {
            return String.format(
                    "📊 METRICAS CONSOLIDADAS DEL DÍA (Stream API & Lambdas):\n" +
                            "• Personas actualmente dentro (En Curso): %d\n" +
                            "• Visitas pendientes de aprobación: %d\n" +
                            "• Visitas finalizadas (Egresaron): %d\n" +
                            "───────────────────────────────────\n" +
                            " TOTAL REGISTROS EVALUADOS: %d",
                    dentroEnCurso, pendientesAprobacion, salidasFinalizadas, totalRegistros);
        }
    }
}
