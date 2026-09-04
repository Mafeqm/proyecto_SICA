package com.sica.service.impl;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Permiso;
import com.sica.model.Rol;
import com.sica.model.Usuario;
import com.sica.service.AuthService;
import com.sica.util.ConexionDB; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthServiceImpl implements AuthService {

    private Usuario usuarioActual;

    @Override
    public Usuario autenticar(String username, String password) throws AccesoDenegadoException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AccesoDenegadoException("Por favor ingrese su usuario y contraseña.");
        }

        // 1. Consultar el usuario y su rol base
        String sqlUsuario = "SELECT u.id, u.nombre, u.email, u.password, u.esta_activo, r.id as rol_id, r.nombre_rol " +
                            "FROM usuarios u " +
                            "INNER JOIN roles r ON u.rol_id = r.id " +
                            "WHERE u.email = ?";

        try (Connection conn = ConexionDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {

            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String passDB = rs.getString("password");
                
                if (!passDB.equals(password)) {
                    throw new AccesoDenegadoException("Credenciales inválidas: Contraseña incorrecta.");
                }

                boolean activo = rs.getBoolean("esta_activo");
                if (!activo) {
                    throw new AccesoDenegadoException("La cuenta se encuentra inactiva o bloqueada.");
                }

                // Construimos el objeto Rol
                Rol rol = new Rol();
                rol.setId(rs.getLong("rol_id")); 
                rol.setNombre(rs.getString("nombre_rol")); 

                // --- NUEVO: 2. Consultar y cargar los permisos de este rol desde MySQL ---
                String sqlPermisos = "SELECT p.id, p.nombre_permiso, p.descripcion " +
                                     "FROM permisos p " +
                                     "INNER JOIN rol_permisos rp ON p.id = rp.permiso_id " +
                                     "WHERE rp.rol_id = ?";
                
                try (PreparedStatement stmtPermisos = conn.prepareStatement(sqlPermisos)) {
                    stmtPermisos.setLong(1, rol.getId());
                    ResultSet rsPermisos = stmtPermisos.executeQuery();
                    
                    while (rsPermisos.next()) {
                        // Usamos el constructor de Permiso de 4 parámetros de tu código original
                        Permiso permiso = new Permiso(
                            rsPermisos.getLong("id"),
                            rsPermisos.getString("nombre_permiso"), 
                            rsPermisos.getString("nombre_permiso"), // Usamos el nombre también como código interno
                            rsPermisos.getString("descripcion")
                        );
                        rol.agregarPermiso(permiso);
                    }
                }
                // -----------------------------------------------------------------------

                // Construimos el objeto Usuario 
                Usuario usuario = new Usuario();
                usuario.setId(rs.getLong("id")); 
                usuario.setUsername(rs.getString("nombre")); 
                usuario.setEmail(rs.getString("email"));
                usuario.setActivo(activo);
                usuario.setRol(rol);

                this.usuarioActual = usuario;
                System.out.println("[AuthService] Sesión iniciada y permisos cargados para: " + usuario.getEmail());
                return usuario;
                
            } else {
                throw new AccesoDenegadoException("Credenciales inválidas: El usuario no existe en la base de datos.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AccesoDenegadoException("Error crítico: No se pudo conectar a la base de datos MySQL.");
        }
    }

    @Override
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    @Override
    public void cerrarSesion() {
        if (usuarioActual != null) {
            System.out.println("[AuthService] Sesión cerrada para: " + usuarioActual.getEmail());
            usuarioActual = null;
        }
    }
}