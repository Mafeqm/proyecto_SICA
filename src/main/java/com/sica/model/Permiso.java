package com.sica.model;

import java.util.Objects;

/**
 * Entidad que representa un Permiso o privilegio del sistema (ej. "PERSONA_CREAR", "VISITA_APROBAR").
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class Permiso {

    private Long id;
    private String nombre;
    private String codigo;
    private String descripcion;

    /**
     * Constructor por defecto.
     */
    public Permiso() {
    }

    /**
     * Constructor parcial sin ID (útil para creación de nuevos registros).
     * 
     * @param nombre Nombre descriptivo del permiso.
     * @param codigo Código único identificador del permiso.
     * @param descripcion Detalle de la función permitida.
     */
    public Permiso(String nombre, String codigo, String descripcion) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    /**
     * Constructor completo.
     * 
     * @param id Identificador único.
     * @param nombre Nombre descriptivo del permiso.
     * @param codigo Código del permiso.
     * @param descripcion Descripción del permiso.
     */
    public Permiso(Long id, String nombre, String codigo, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(id, permiso.id) || Objects.equals(codigo, permiso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigo);
    }

    @Override
    public String toString() {
        return "Permiso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + codigo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
