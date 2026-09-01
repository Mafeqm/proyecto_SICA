package com.sica.strategy;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;

/**
 * Interfaz Strategy (Patrón de Diseño Strategy).
 * Define la abstracción para los distintos algoritmos de flujo de acceso en el
 * sistema SICA.
 * 
 * Principios SOLID:
 * - Open/Closed Principle (OCP): Permite extender nuevos flujos de ingreso (ej.
 * Acceso VIP, Acceso Vehicular)
 * sin modificar las clases de servicio existentes.
 * - Single Responsibility Principle (SRP): Cada implementación concreta
 * encapsula únicamente la regla del flujo correspondiente.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface EstrategiaAcceso {

    /**
     * Procesa el acceso de una persona según la variante de la estrategia.
     * 
     * @param persona         Persona física que intenta acceder.
     * @param visitaDAO       Componente DAO para persistencia de datos.
     * @param usuarioRegistro Usuario que registra el acceso.
     * @param motivo          Motivo del ingreso.
     * @param observaciones   Observaciones adicionales.
     * @return Objeto Visita procesado.
     */
    Visita procesarAcceso(Persona persona, VisitaDAO visitaDAO, Usuario usuarioRegistro, String motivo,
            String observaciones);

    /**
     * Retorna la denominación del tipo de estrategia.
     */
    String getNombreEstrategia();
}
