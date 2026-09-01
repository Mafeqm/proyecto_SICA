package com.sica.observer;

import com.sica.model.Visita;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Subject / Observable (Patrón de Diseño Observer).
 * Mantiene la lista de observadores registrados (funcionarios, controladores de
 * pantalla, etc.)
 * y transmite las notificaciones cuando ocurren eventos de ingreso no
 * anunciado.
 * 
 * Principios SOLID:
 * - Single Responsibility Principle (SRP): Gestión exclusiva de suscriptores y
 * emisión de notificaciones.
 * - Open/Closed Principle (OCP): Permite agregar nuevos tipos de observadores
 * sin modificar este código.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class NotificadorVisitaSubject {

    private static NotificadorVisitaSubject instancia;
    private final List<ObserverFuncionario> observadores;

    public NotificadorVisitaSubject() {
        this.observadores = new ArrayList<>();
    }

    /**
     * Singleton opcional para tener un Subject centralizado en la aplicación.
     */
    public static synchronized NotificadorVisitaSubject getInstanciaGlobal() {
        if (instancia == null) {
            instancia = new NotificadorVisitaSubject();
        }
        return instancia;
    }

    /**
     * Suscribe un nuevo observador.
     * 
     * @param observer Instancia del observador.
     */
    public void registrarObserver(ObserverFuncionario observer) {
        if (observer != null && !observadores.contains(observer)) {
            observadores.add(observer);
            System.out.println("[Subject Observer] Observador registrado exitosamente.");
        }
    }

    /**
     * Elimina la suscripción de un observador.
     * 
     * @param observer Instancia a remover.
     */
    public void removerObserver(ObserverFuncionario observer) {
        if (observadores.remove(observer)) {
            System.out.println("[Subject Observer] Observador desregistrado.");
        }
    }

    /**
     * Notifica a todos los observadores registrados sobre una visita pendiente.
     * 
     * @param visita Visita que requiere aprobación.
     */
    public void notificarObservadores(Visita visita) {
        System.out.println("[Subject Observer] Emitiendo notificación a " + observadores.size() + " observador(es)...");
        for (ObserverFuncionario obs : observadores) {
            obs.notificarVisitaPendiente(visita);
        }
    }

    public int getCantidadObservadores() {
        return observadores.size();
    }
}
