package com.sica.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un registro de Visita o Control de Acceso en el
 * sistema SICA.
 * Controla el ciclo de vida del ingreso, permanencia y salida de una persona.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class Visita {

    private Long id;
    private Persona persona;
    private Usuario usuarioRegistro;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private String motivo;
    private String estado; // PENDIENTE, EN_CURSO, FINALIZADA, CANCELADA
    private String observaciones;

    /**
     * Constructor por defecto. Asigna estado EN_CURSO y fechaEntrada actual.
     */
    public Visita() {
        this.fechaEntrada = LocalDateTime.now();
        this.estado = "EN_CURSO";
    }

    /**
     * Constructor parcial para registrar nueva visita.
     * 
     * @param persona         Persona que ingresa.
     * @param usuarioRegistro Usuario/Guardia que realiza el registro.
     * @param motivo          Motivo de la visita.
     * @param observaciones   Notas o comentarios adicionales.
     */
    public Visita(Persona persona, Usuario usuarioRegistro, String motivo, String observaciones) {
        this();
        this.persona = persona;
        this.usuarioRegistro = usuarioRegistro;
        this.motivo = motivo;
        this.observaciones = observaciones;
    }

    /**
     * Constructor completo.
     * 
     * @param id              Identificador único.
     * @param persona         Persona que ingresa.
     * @param usuarioRegistro Usuario registrador.
     * @param fechaEntrada    Fecha y hora de ingreso.
     * @param fechaSalida     Fecha y hora de salida.
     * @param motivo          Motivo del acceso.
     * @param estado          Estado de la visita.
     * @param observaciones   Comentarios o novedades.
     */
    public Visita(Long id, Persona persona, Usuario usuarioRegistro, LocalDateTime fechaEntrada,
            LocalDateTime fechaSalida, String motivo, String estado, String observaciones) {
        this.id = id;
        this.persona = persona;
        this.usuarioRegistro = usuarioRegistro;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.motivo = motivo;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Usuario getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(Usuario usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Finaliza la visita registrando la hora de salida actual y cambiando su
     * estado.
     */
    public void registrarSalida() {
        this.fechaSalida = LocalDateTime.now();
        this.estado = "FINALIZADA";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Visita visita = (Visita) o;
        return Objects.equals(id, visita.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Visita{" +
                "id=" + id +
                ", persona=" + (persona != null ? persona.getNombreCompleto() : "N/A") +
                ", fechaEntrada=" + fechaEntrada +
                ", fechaSalida=" + fechaSalida +
                ", estado='" + estado + '\'' +
                ", motivo='" + motivo + '\'' +
                '}';
    }
}
