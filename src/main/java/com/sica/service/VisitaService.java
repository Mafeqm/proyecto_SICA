package com.sica.service;

import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.strategy.EstrategiaAcceso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de la Capa de Servicio (Service Layer) para la gestión de Visitas y
 * Control de Acceso.
 * Define la API de negocio y operaciones de procesamiento, filtrado y
 * auditoría.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface VisitaService {

    /**
     * Registra el ingreso de una persona delegando el flujo específico a la
     * estrategia de acceso provista.
     * 
     * @param persona         Persona que ingresa.
     * @param estrategia      Algoritmo del flujo de acceso (Patrón Strategy).
     * @param usuarioRegistro Usuario/Guardia que realiza el registro.
     * @param motivo          Motivo de la visita.
     * @param observaciones   Comentarios o notas.
     * @return Visita resultante del procesamiento.
     */
    Visita registrarAcceso(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioRegistro, String motivo,
            String observaciones);

    /**
     * Registra el egreso de las instalaciones para una visita activa.
     * 
     * @param visitaId      Identificador de la visita.
     * @param fechaSalida   Fecha y hora exacta de salida.
     * @param observaciones Notas al momento de salida.
     * @return true si la salida fue registrada con éxito.
     */
    boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones);

    /**
     * Obtiene el listado completo de visitas desde el repositorio.
     * 
     * @return Lista general de visitas.
     */
    List<Visita> listarTodas();

    /**
     * Filtra una lista de visitas por su estado utilizando la API Stream y
     * expresiones Lambda.
     * 
     * @param visitas Lista origen de visitas.
     * @param estado  Estado a filtrar (ej. "EN_CURSO", "PENDIENTE", "FINALIZADA").
     * @return Lista filtrada.
     */
    List<Visita> filtrarVisitasPorEstado(List<Visita> visitas, String estado);

    /**
     * Obtiene las visitas asociadas a una persona específica mediante Stream API.
     * 
     * @param visitas   Lista origen.
     * @param personaId Identificador de la persona.
     * @return Lista de visitas de la persona.
     */
    List<Visita> obtenerVisitasRecientesPorPersona(List<Visita> visitas, Long personaId);

    /**
     * Cuenta el total de visitas en curso utilizando Stream API.
     * 
     * @param visitas Lista origen de visitas.
     * @return Cantidad de visitas activas.
     */
    long contarVisitasActivas(List<Visita> visitas);
}
