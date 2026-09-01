package com.sica.observer;

import com.sica.model.Visita;

/**
 * Interfaz Observer (Patrón de Diseño Observer).
 * Define la acción de actualización que deben implementar los suscriptores (ej.
 * pantallas de funcionarios,
 * servicios de mensajería, etc.) cuando se genera un evento de acceso que
 * requiere atención inmediata.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface ObserverFuncionario {

    /**
     * Método invocado automáticamente por el Subject cuando se registra una nueva
     * visita
     * que se encuentra en estado "PENDIENTE" de aprobación.
     * 
     * @param visita Objeto Visita registrado en estado pendiente.
     */
    void notificarVisitaPendiente(Visita visita);
}
