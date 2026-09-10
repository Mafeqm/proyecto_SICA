package com.sica.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un Rol de usuario dentro del sistema SICA (ej. "ADMINISTRADOR", "GUARDIA_SEGURIDAD").
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class Rol {

    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private List<Permiso> permisos;

    /**
     * Constructor por defecto. Inicializa la lista de permisos vacía y la fecha actual.
     */
    public Rol() {
        this.permisos = new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor sin ID.
     * 
     * @param nombre Nombre del rol.
     * @param descripcion Descripción de los alcances del rol.
     */
    public Rol(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Constructor con ID y datos básicos.
     * 
     * @param id Identificador único.
     * @param nombre Nombre del rol.
     * @param descripcion Descripción del rol.
     */
    public Rol(Long id, String nombre, String descripcion) {
        this(nombre, descripcion);
        this.id = id;
    }

    /**
     * Constructor completo.
     * 
     * @param id Identificador único.
     * @param nombre Nombre del rol.
     * @param descripcion Descripción del rol.
     * @param fechaCreacion Fecha y hora de creación.
     */
    public Rol(Long id, String nombre, String descripcion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.permisos = new ArrayList<>();
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos != null ? permisos : new ArrayList<>();
    }

    public void agregarPermiso(Permiso permiso) {
        if (permiso != null && !this.permisos.contains(permiso)) {
            this.permisos.add(permiso);
        }
    }

    public void removerPermiso(Permiso permiso) {
        this.permisos.remove(permiso);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(id, rol.id) || Objects.equals(nombre, rol.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", totalPermisos=" + (permisos != null ? permisos.size() : 0) +
                '}';
    }
}
