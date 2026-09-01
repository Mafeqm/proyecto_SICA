package com.sica.observer;

import com.sica.model.Visita;

/**
 * Implementación concreta del Patrón Observer.
 * Simula el módulo o vista del Funcionario que recibe avisos de visitantes no
 * anunciados en tiempo real.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class FuncionarioObserverImpl implements ObserverFuncionario {

    private String nombreFuncionario;
    private String departamento;

    public FuncionarioObserverImpl(String nombreFuncionario, String departamento) {
        this.nombreFuncionario = nombreFuncionario;
        this.departamento = departamento;
    }

    @Override
    public void notificarVisitaPendiente(Visita visita) {
        String visitante = (visita != null && visita.getPersona() != null)
                ? visita.getPersona().getNombreCompleto()
                : "Desconocido";

        String documento = (visita != null && visita.getPersona() != null)
                ? visita.getPersona().getTipoDocumento() + " " + visita.getPersona().getNumeroDocumento()
                : "N/A";

        System.out.println("[NOTIFICACIÓN TIEMPO REAL - OBSERVER] 🔔 ¡Atención Funcionario "
                + nombreFuncionario + " (" + departamento + ")!");
        System.out.println("   --> Tienes una nueva visita no anunciada PENDIENTE DE APROBACIÓN.");
        System.out.println("   --> Visitante: " + visitante + " (Doc: " + documento + ")");
        System.out.println("   --> Motivo: " + (visita != null ? visita.getMotivo() : "Sin especificar"));
        System.out.println("   --> Fecha/Hora: " + (visita != null ? visita.getFechaEntrada() : "Ahora"));
    }

    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    public String getDepartamento() {
        return departamento;
    }
}
