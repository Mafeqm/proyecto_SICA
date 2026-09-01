package com.sica.dao;

import com.sica.model.Persona;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define las operaciones de acceso a datos (DAO) para la entidad
 * Persona.
 * Aplica los principios CRUD y métodos de consulta especializados requeridos
 * por el dominio.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface PersonaDAO {

    /**
     * Registra una nueva persona en el sistema.
     * 
     * @param persona Objeto Persona con los datos a insertar.
     * @return Persona insertada con su ID asignado.
     */
    Persona crear(Persona persona);

    /**
     * Busca una persona por su identificador único.
     * 
     * @param id Identificador único de la persona.
     * @return Optional conteniendo la Persona si existe, o vacío en caso contrario.
     */
    Optional<Persona> obtenerPorId(Long id);

    /**
     * Obtiene la lista completa de personas registradas.
     * 
     * @return Lista de personas.
     */
    List<Persona> listarTodos();

    /**
     * Actualiza la información de una persona existente.
     * 
     * @param persona Objeto Persona con los datos modificados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    boolean actualizar(Persona persona);

    /**
     * Elimina físicamente o lógicamente una persona del sistema.
     * 
     * @param id Identificador de la persona.
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    boolean eliminar(Long id);

    /**
     * Busca una persona específicamente por tipo y número de documento (Flujo de
     * Control de Acceso).
     * 
     * @param tipoDocumento   Tipo de documento (CC, CE, PASAPORTE, etc.).
     * @param numeroDocumento Número del documento de identidad.
     * @return Optional con la persona encontrada.
     */
    Optional<Persona> buscarPorDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Lista todas las personas asociadas a una categoría o tipo de persona
     * específico.
     * 
     * @param tipoPersona Tipo de persona (EMPLEADO, VISITANTE, CONTRATISTA).
     * @return Lista de personas pertenecientes al tipo especificado.
     */
    List<Persona> listarPorTipo(String tipoPersona);

    /**
     * Activa o desactiva la disponibilidad de acceso de una persona.
     * 
     * @param id     Identificador de la persona.
     * @param activo Nuevo estado (true = activo, false = inactivo/bloqueado).
     * @return true si la operación tuvo éxito.
     */
    boolean cambiarEstadoActivo(Long id, boolean activo);
}
