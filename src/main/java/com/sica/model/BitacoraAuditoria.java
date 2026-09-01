package com.sica.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un registro en la Bitácora de Auditoría del sistema
 * SICA.
 * Garantiza la trazabilidad y no repudio de acciones críticas realizadas por
 * los usuarios.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class BitacoraAuditoria {

    private Long id;
    private Usuario usuario; // Usuario que ejecutó la acción
    private String accion; // ej. INSERT, UPDATE, DELETE, LOGIN, LOGOUT
    private String tablaAfectada; // ej. persona, visita, usuario
    private Long registroId; // ID del registro afectado en la tabla objetivo
    private String detalle; // Descripción o JSON diff del cambio
    private LocalDateTime fechaHora;
    private String direccionIP;

    /**
     * Constructor por defecto. Registra la fecha y hora actual automáticamente.
     */
    public BitacoraAuditoria() {
        this.fechaHora = LocalDateTime.now();
    }

    /**
     * Constructor parcial para registrar evento de auditoría.
     * 
     * @param usuario       Usuario responsable de la acción.
     * @param accion        Operación realizada (INSERT, UPDATE, DELETE, etc.).
     * @param tablaAfectada Nombre de la entidad/tabla impactada.
     * @param registroId    Identificador del registro modificado.
     * @param detalle       Información complementaria sobre el cambio.
     * @param direccionIP   Dirección IP del cliente.
     */
    public BitacoraAuditoria(Usuario usuario, String accion, String tablaAfectada,
            Long registroId, String detalle, String direccionIP) {
        this();
        this.usuario = usuario;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.registroId = registroId;
        this.detalle = detalle;
        this.direccionIP = direccionIP;
    }

    /**
     * Constructor completo.
     * 
     * @param id            Identificador único del log de auditoría.
     * @param usuario       Usuario autor.
     * @param accion        Acción ejecutada.
     * @param tablaAfectada Tabla afectada.
     * @param registroId    ID de la entidad afectada.
     * @param detalle       Detalle amplio de la acción.
     * @param fechaHora     Fecha y hora del evento.
     * @param direccionIP   IP origen de la petición.
     */
    public BitacoraAuditoria(Long id, Usuario usuario, String accion, String tablaAfectada,
            Long registroId, String detalle, LocalDateTime fechaHora, String direccionIP) {
        this.id = id;
        this.usuario = usuario;
        this.accion = accion;
        this.tablaAfectada = tablaAfectada;
        this.registroId = registroId;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
        this.direccionIP = direccionIP;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public void setDireccionIP(String direccionIP) {
        this.direccionIP = direccionIP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BitacoraAuditoria log = (BitacoraAuditoria) o;
        return Objects.equals(id, log.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BitacoraAuditoria{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : "SISTEMA") +
                ", accion='" + accion + '\'' +
                ", tablaAfectada='" + tablaAfectada + '\'' +
                ", registroId=" + registroId +
                ", fechaHora=" + fechaHora +
                ", direccionIP='" + direccionIP + '\'' +
                '}';
    }
}
