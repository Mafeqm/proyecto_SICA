package com.sica.service.impl;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Permiso;
import com.sica.model.Rol;
import com.sica.model.Usuario;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.AuthService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de la Capa de Servicio para la autenticación de Usuarios en
 * SICA.
 * Configura usuarios predeterminados con roles y permisos específicos para la
 * validación del Proxy RBAC.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class AuthServiceImpl implements AuthService {

    private final Map<String, Usuario> usuariosDB = new HashMap<>();
    private final Map<String, String> credencialesDB = new HashMap<>();
    private Usuario usuarioActual;

    public AuthServiceImpl() {
        inicializarUsuariosPredeterminados();
    }

    /**
     * Inicializa usuarios con distintos niveles de privilegios RBAC.
     */
    private void inicializarUsuariosPredeterminados() {
        // Permisos
        Permiso permRegistrar = new Permiso(1L, "Registrar Accesos",
                SeguridadVisitaServiceProxy.PERMISO_REGISTRAR_ACCESO, "Permite registrar ingresos de visitas");
        Permiso permSalida = new Permiso(2L, "Registrar Salida", SeguridadVisitaServiceProxy.PERMISO_REGISTRAR_SALIDA,
                "Permite registrar salidas de visitas");
        Permiso permConsultar = new Permiso(3L, "Consultar Visitas",
                SeguridadVisitaServiceProxy.PERMISO_CONSULTAR_VISITAS, "Permite consultar listas de visitas");
        Permiso permAprobar = new Permiso(4L, "Aprobar Visita", "VISITA_APROBAR",
                "Permite aprobar visitas no anunciadas");

        // Rol ADMINISTRADOR (Acceso Maestro)
        Rol rolAdmin = new Rol(1L, "ADMINISTRADOR", "Acceso total al sistema", LocalDateTime.now());
        rolAdmin.agregarPermiso(new Permiso(99L, "Acceso Total", "ACCESO_TOTAL", "Acceso a todo"));

        // Rol GUARDIA (Solo registro de entrada/salida y consulta)
        Rol rolGuardia = new Rol(2L, "GUARDIA_SEGURIDAD", "Operaciones de control de accesos", LocalDateTime.now());
        rolGuardia.agregarPermiso(permRegistrar);
        rolGuardia.agregarPermiso(permSalida);
        rolGuardia.agregarPermiso(permConsultar);

        // Rol FUNCIONARIO (Solo aprobación de visitas y consulta, sin registro de
        // entradas)
        Rol rolFuncionario = new Rol(3L, "FUNCIONARIO_APROBADOR", "Aprobador de invitados", LocalDateTime.now());
        rolFuncionario.agregarPermiso(permAprobar);
        rolFuncionario.agregarPermiso(permConsultar);

        // Rol OPERADOR_LECTURA (Sin permiso para registrar entrada ni salida)
        Rol rolLectura = new Rol(4L, "OPERADOR_LECTURA", "Solo consulta", LocalDateTime.now());
        rolLectura.agregarPermiso(permConsultar);

        // Crear Usuarios
        Usuario admin = new Usuario(1L, "admin", "admin123", "admin@sica.com", true, LocalDateTime.now(), rolAdmin);
        Usuario guardia = new Usuario(2L, "guardia", "guardia123", "guardia@sica.com", true, LocalDateTime.now(),
                rolGuardia);
        Usuario funcionario = new Usuario(3L, "funcionario", "func123", "func@sica.com", true, LocalDateTime.now(),
                rolFuncionario);
        Usuario sinPermisos = new Usuario(4L, "operador", "operador123", "operador@sica.com", true, LocalDateTime.now(),
                rolLectura);

        usuariosDB.put("admin", admin);
        credencialesDB.put("admin", "admin123");

        usuariosDB.put("guardia", guardia);
        credencialesDB.put("guardia", "guardia123");

        usuariosDB.put("funcionario", funcionario);
        credencialesDB.put("funcionario", "func123");

        usuariosDB.put("operador", sinPermisos);
        credencialesDB.put("operador", "operador123");
    }

    @Override
    public Usuario autenticar(String username, String password) throws AccesoDenegadoException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AccesoDenegadoException("Por favor ingrese su usuario y contraseña.");
        }

        String userKey = username.trim().toLowerCase();
        if (!usuariosDB.containsKey(userKey)) {
            throw new AccesoDenegadoException(
                    "Credenciales inválidas: El usuario '" + username + "' no existe en el sistema.");
        }

        String passReal = credencialesDB.get(userKey);
        if (!passReal.equals(password)) {
            throw new AccesoDenegadoException(
                    "Credenciales inválidas: Contraseña incorrecta para el usuario '" + username + "'.");
        }

        Usuario usuario = usuariosDB.get(userKey);
        if (!usuario.isActivo()) {
            throw new AccesoDenegadoException(usuario.getUsername(), "LOGIN",
                    "La cuenta de usuario '" + username + "' se encuentra bloqueada/inactiva.");
        }

        this.usuarioActual = usuario;
        System.out.println("[AuthService] Usuario autenticado exitosamente: " + usuario.getUsername() + " [Rol: "
                + usuario.getRol().getNombre() + "]");
        return usuario;
    }

    @Override
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    @Override
    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("[AuthService] Sesión cerrada para usuario: " + usuarioActual.getUsername());
            usuarioActual = null;
        }
    }
}
