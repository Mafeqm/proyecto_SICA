package com.sica.dao.impl;

import com.sica.dao.PersonaDAO;
import com.sica.model.Persona;
import com.sica.util.ConexionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC específica para MySQL de la interfaz PersonaDAO.
 * Aplica el patrón de diseño DAO (Data Access Object), desacoplando la lógica
 * de negocio del motor de base de datos MySQL específico.
 * 
 * Principios SOLID y buenas prácticas:
 * - Single Responsibility Principle (SRP): Gestión exclusiva del ciclo de vida
 * de la entidad Persona en BD.
 * - Manejo seguro de memoria e hilos usando try-with-resources.
 * - Prevención de inyección SQL mediante PreparedStatement.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class PersonaDAOMySQLImpl implements PersonaDAO {

    private static final String SQL_INSERT = "INSERT INTO persona (tipo_documento, numero_documento, nombres, apellidos, email, telefono, tipo_persona, activo, fecha_registro) "
            +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID = "SELECT id, tipo_documento, numero_documento, nombres, apellidos, email, telefono, tipo_persona, activo, fecha_registro "
            +
            "FROM persona WHERE id = ?";

    private static final String SQL_SELECT_ALL = "SELECT id, tipo_documento, numero_documento, nombres, apellidos, email, telefono, tipo_persona, activo, fecha_registro "
            +
            "FROM persona ORDER BY apellidos, nombres";

    private static final String SQL_UPDATE = "UPDATE persona SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, email = ?, telefono = ?, tipo_persona = ?, activo = ? "
            +
            "WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM persona WHERE id = ?";

    private static final String SQL_SELECT_BY_DOC = "SELECT id, tipo_documento, numero_documento, nombres, apellidos, email, telefono, tipo_persona, activo, fecha_registro "
            +
            "FROM persona WHERE tipo_documento = ? AND numero_documento = ?";

    private static final String SQL_SELECT_BY_TIPO = "SELECT id, tipo_documento, numero_documento, nombres, apellidos, email, telefono, tipo_persona, activo, fecha_registro "
            +
            "FROM persona WHERE tipo_persona = ? ORDER BY apellidos, nombres";

    private static final String SQL_UPDATE_ACTIVO = "UPDATE persona SET activo = ? WHERE id = ?";

    @Override
    public Persona crear(Persona persona) {
        if (persona == null)
            return null;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, persona.getTipoDocumento());
            stmt.setString(2, persona.getNumeroDocumento());
            stmt.setString(3, persona.getNombres());
            stmt.setString(4, persona.getApellidos());
            stmt.setString(5, persona.getEmail());
            stmt.setString(6, persona.getTelefono());
            stmt.setString(7, persona.getTipoPersona());
            stmt.setBoolean(8, persona.isActivo());

            LocalDateTime fechaReg = persona.getFechaRegistro() != null ? persona.getFechaRegistro()
                    : LocalDateTime.now();
            stmt.setTimestamp(9, Timestamp.valueOf(fechaReg));

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        persona.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al insertar persona: " + e.getMessage());
        }
        return persona;
    }

    @Override
    public Optional<Persona> obtenerPorId(Long id) {
        if (id == null)
            return Optional.empty();

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al consultar persona por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Persona> listarTodos() {
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                personas.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al listar todas las personas: " + e.getMessage());
        }
        return personas;
    }

    @Override
    public boolean actualizar(Persona persona) {
        if (persona == null || persona.getId() == null)
            return false;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, persona.getTipoDocumento());
            stmt.setString(2, persona.getNumeroDocumento());
            stmt.setString(3, persona.getNombres());
            stmt.setString(4, persona.getApellidos());
            stmt.setString(5, persona.getEmail());
            stmt.setString(6, persona.getTelefono());
            stmt.setString(7, persona.getTipoPersona());
            stmt.setBoolean(8, persona.isActivo());
            stmt.setLong(9, persona.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al actualizar persona: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Long id) {
        if (id == null)
            return false;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al eliminar persona: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
        if (tipoDocumento == null || numeroDocumento == null)
            return Optional.empty();

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_DOC)) {

            stmt.setString(1, tipoDocumento);
            stmt.setString(2, numeroDocumento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al buscar por documento: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Persona> listarPorTipo(String tipoPersona) {
        List<Persona> personas = new ArrayList<>();
        if (tipoPersona == null)
            return personas;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_TIPO)) {

            stmt.setString(1, tipoPersona);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al listar por tipo de persona: " + e.getMessage());
        }
        return personas;
    }

    @Override
    public boolean cambiarEstadoActivo(Long id, boolean activo) {
        if (id == null)
            return false;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_ACTIVO)) {

            stmt.setBoolean(1, activo);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PersonaDAOMySQLImpl] Error al cambiar estado activo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapea una fila actual del ResultSet a una instancia del objeto Persona.
     */
    private Persona mapearResultSet(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getLong("id"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNumeroDocumento(rs.getString("numero_documento"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setEmail(rs.getString("email"));
        p.setTelefono(rs.getString("telefono"));
        p.setTipoPersona(rs.getString("tipo_persona"));
        p.setActivo(rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            p.setFechaRegistro(ts.toLocalDateTime());
        }
        return p;
    }
}
