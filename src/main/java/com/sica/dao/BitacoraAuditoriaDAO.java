package com.sica.dao;

import com.sica.model.BitacoraAuditoria;
import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos para la Bitácora de
 * Auditoría.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface BitacoraAuditoriaDAO {

    /**
     * Inserta un nuevo registro de auditoría en la base de datos.
     * 
     * @param bitacora Objeto con la información del evento a auditar.
     * @return Objeto BitacoraAuditoria guardado con ID asignado.
     */
    BitacoraAuditoria registrar(BitacoraAuditoria bitacora);

    /**
     * Obtiene todos los registros de la bitácora de auditoría.
     * 
     * @return Lista de eventos auditados.
     */
    List<BitacoraAuditoria> listarTodos();

    /**
     * Obtiene los eventos de auditoría generados por un usuario específico.
     * 
     * @param usuarioId Identificador del usuario.
     * @return Lista de eventos del usuario.
     */
    List<BitacoraAuditoria> listarPorUsuario(Long usuarioId);
}
