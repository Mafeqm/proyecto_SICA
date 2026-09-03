package com.sica.exception;

/**
 * Excepción personalizada que se dispara cuando un usuario intenta ejecutar una
 * acción
 * para la cual no posee los permisos RBAC (Role-Based Access Control)
 * requeridos.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AccesoDenegadoException extends Exception {

    private final String usuario;
    private final String permisoRequerido;

    /**
     * Constructor con mensaje explicativo.
     * 
     * @param mensaje Mensaje del error de seguridad.
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
        this.usuario = "DESCONOCIDO";
        this.permisoRequerido = "N/A";
    }

    /**
     * Constructor estructurado para auditoría y visualización de permisos
     * faltantes.
     * 
     * @param usuario          Nombre de usuario que intentó la acción.
     * @param permisoRequerido Código del permiso RBAC requerido.
     * @param mensaje          Mensaje detallado.
     */
    public AccesoDenegadoException(String usuario, String permisoRequerido, String mensaje) {
        super(mensaje);
        this.usuario = usuario;
        this.permisoRequerido = permisoRequerido;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPermisoRequerido() {
        return permisoRequerido;
    }
}
