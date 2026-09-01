package com.sica.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa a un Usuario del sistema SICA con credenciales y
 * asignación de Rol.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class Usuario {

    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private Rol rol;

    /**
     * Constructor por defecto. Asigna la fecha actual y estado activo por defecto.
     */
    public Usuario() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor parcial sin ID.
     * 
     * @param username     Nombre de usuario para login.
     * @param passwordHash Hash de la contraseña.
     * @param email        Correo electrónico.
     * @param rol          Rol asignado al usuario.
     */
    public Usuario(String username, String passwordHash, String email, Rol rol) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.rol = rol;
    }

    /**
     * Constructor completo.
     * 
     * @param id            Identificador único.
     * @param username      Nombre de usuario.
     * @param passwordHash  Hash de la contraseña.
     * @param email         Correo electrónico.
     * @param activo        Estado del usuario en el sistema.
     * @param fechaCreacion Fecha y hora de creación.
     * @param rol           Rol asignado.
     */
    public Usuario(Long id, String username, String passwordHash, String email, boolean activo,
            LocalDateTime fechaCreacion, Rol rol) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.rol = rol;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) || Objects.equals(username, usuario.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", rol=" + (rol != null ? rol.getNombre() : "SIN_ROL") +
                '}';
    }
}
