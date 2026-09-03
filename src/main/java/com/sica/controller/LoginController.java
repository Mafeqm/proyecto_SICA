package com.sica.controller;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Usuario;
import com.sica.service.AuthService;

/**
 * Controlador para la vista de Autenticación / Login (Patrón de Diseño MVC).
 * 
 * Principios SOLID:
 * - Dependency Inversion Principle (DIP): Depende de la interfaz de abstracción
 * AuthService, no de implementaciones concretas.
 * - Single Responsibility Principle (SRP): Orquestación exclusiva entre la
 * Vista de Login y el Servicio de Autenticación.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class LoginController {

    private final AuthService authService;

    /**
     * Inyección de Dependencias por Constructor.
     * 
     * @param authService Abstracción del servicio de autenticación.
     */
    public LoginController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("El controlador requiere una instancia válida de AuthService.");
        }
        this.authService = authService;
    }

    /**
     * Procesa la solicitud de inicio de sesión de la vista.
     * 
     * @param username Usuario ingresado.
     * @param password Contraseña ingresada.
     * @return Usuario autenticado si las credenciales y estado son correctos.
     * @throws AccesoDenegadoException Si las credenciales o el estado de la cuenta
     *                                 fallan.
     */
    public Usuario iniciarSesion(String username, String password) throws AccesoDenegadoException {
        return authService.autenticar(username, password);
    }

    /**
     * Cierra la sesión activa.
     */
    public void cerrarSesion() {
        authService.cerrarSesion();
    }

    public Usuario getUsuarioSesionActual() {
        return authService.getUsuarioActual();
    }
}
