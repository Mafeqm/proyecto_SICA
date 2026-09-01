package com.sica.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un Incidente o Novedad de seguridad dentro del sistema
 * SICA.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class Incidente {

    private Long id;
    private Persona persona; // Persona involucrada (opcional/nullable)
    private Usuario usuarioReporta; // Usuario del sistema que registró el incidente
    private LocalDateTime fechaHora;
    private String titulo;
    private String descripcion;
    private String nivelGravedad; // BAJO, MEDIO, ALTO, CRITICO
    private String estado; // REGISTRADO, EN_INVESTIGACION, RESUELTO, CERRADO

    /**
     * Constructor por defecto. Asigna la fecha y hora actual y estado REGISTRADO.
     */
    public Incidente() {
        this.fechaHora = LocalDateTime.now();
        this.nivelGravedad = "MEDIO";
        this.estado = "REGISTRADO";
    }

    /**
     * Constructor para reportar un nuevo incidente.
     * 
     * @param persona        Persona involucrada (puede ser null).
     * @param usuarioReporta Usuario que reporta.
     * @param titulo         Título o resumen breve.
     * @param descripcion    Detalle amplio de los hechos.
     * @param nivelGravedad  Severidad del evento (BAJO, MEDIO, ALTO, CRITICO).
     */
    public Incidente(Persona persona, Usuario usuarioReporta, String titulo, String descripcion, String nivelGravedad) {
        this();
        this.persona = persona;
        this.usuarioReporta = usuarioReporta;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivelGravedad = nivelGravedad;
    }

    /**
     * Constructor completo.
     * 
     * @param id             Identificador único.
     * @param persona        Persona asociada.
     * @param usuarioReporta Usuario reportador.
     * @param fechaHora      Fecha y hora del evento.
     * @param titulo         Título del incidente.
     * @param descripcion    Detalle de la novedad.
     * @param nivelGravedad  Nivel de severidad.
     * @param estado         Estado del trámite del incidente.
     */
    public Incidente(Long id, Persona persona, Usuario usuarioReporta, LocalDateTime fechaHora,
            String titulo, String descripcion, String nivelGravedad, String estado) {
        this.id = id;
        this.persona = persona;
        this.usuarioReporta = usuarioReporta;
        this.fechaHora = fechaHora;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivelGravedad = nivelGravedad;
        this.estado = estado;
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

    public Usuario getUsuarioReporta() {
        return usuarioReporta;
    }

    public void setUsuarioReporta(Usuario usuarioReporta) {
        this.usuarioReporta = usuarioReporta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNivelGravedad() {
        return nivelGravedad;
    }

    public void setNivelGravedad(String nivelGravedad) {
        this.nivelGravedad = nivelGravedad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Incidente incidente = (Incidente) o;
        return Objects.equals(id, incidente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", nivelGravedad='" + nivelGravedad + '\'' +
                ", fechaHora=" + fechaHora +
                ", estado='" + estado + '\'' +
                '}';
    }
}
