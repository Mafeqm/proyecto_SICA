package com.sica.service;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Usuario;

/**
 * Interfaz de Servicio para Autenticación e Inicio de Sesión de Usuarios.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public interface AuthService {

    /**
     * Autentica un usuario verificando sus credenciales de acceso.
     * 
     * @param username Nombre de usuario.
     * @param password Contraseña plana (se valida contra el hash).
     * @return Objeto Usuario autenticado con su Rol y Permisos cargados.
     * @throws AccesoDenegadoException Si la cuenta está inactiva o credenciales son
     *                                 inválidas.
     */
    Usuario autenticar(String username, String password) throws AccesoDenegadoException;

    /**
     * Retorna el usuario actualmente autenticado en la sesión.
     */
    Usuario getUsuarioActual();

    /**
     * Cierra la sesión activa.
     */
    void cerrarSesion();
}
