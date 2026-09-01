package com.sica.dao;

import com.sica.model.Visita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define las operaciones de acceso a datos (DAO) para la entidad
 * Visita.
 * Incluye métodos para la gestión de ingresos, egresos y auditoría del
 * historial de accesos.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface VisitaDAO {

    /**
     * Registra un nuevo ingreso de visita en la base de datos.
     * 
     * @param visita Objeto Visita con los datos del ingreso.
     * @return Visita creada con su identificador generado.
     */
    Visita crear(Visita visita);

    /**
     * Busca un registro de visita por su identificador único.
     * 
     * @param id Identificador de la visita.
     * @return Optional conteniendo la Visita si existe.
     */
    Optional<Visita> obtenerPorId(Long id);

    /**
     * Lista el historial completo de visitas registradas en el sistema.
     * 
     * @return Lista general de visitas.
     */
    List<Visita> listarTodos();

    /**
     * Actualiza los datos de un registro de visita.
     * 
     * @param visita Objeto Visita con la información actualizada.
     * @return true si se actualizó correctamente, false en caso contrario.
     */
    boolean actualizar(Visita visita);

    /**
     * Elimina un registro de visita del sistema.
     * 
     * @param id Identificador de la visita.
     * @return true si la eliminación tuvo éxito.
     */
    boolean eliminar(Long id);

    /**
     * Busca si una persona específica tiene una visita activa en curso (sin marcar
     * salida).
     * Requisito fundamental para validaciones de ingreso y prevención de doble
     * entrada.
     * 
     * @param personaId Identificador de la persona.
     * @return Optional con la última visita con estado "EN_CURSO" o "PENDIENTE".
     */
    Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long personaId);

    /**
     * Obtiene la lista de todas las personas que actualmente están dentro de las
     * instalaciones.
     * 
     * @return Lista de visitas en estado "EN_CURSO".
     */
    List<Visita> listarVisitasActivas();

    /**
     * Consulta el historial de visitas dentro de un rango de fechas/horas
     * determinado.
     * 
     * @param inicio Fecha y hora inicial del filtro.
     * @param fin    Fecha y hora final del filtro.
     * @return Lista de visitas filtradas por fecha.
     */
    List<Visita> listarVisitasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Registra la salida de una visita activa actualizando su fecha de salida y
     * estado.
     * 
     * @param visitaId      Identificador de la visita.
     * @param fechaSalida   Fecha y hora exacta de la salida.
     * @param observaciones Comentarios o notas al momento del egreso.
     * @return true si se completó el registro de salida correctamente.
     */
    boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones);
}
