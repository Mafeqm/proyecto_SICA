package com.sica.dao.impl;

import com.sica.dao.VisitaDAO;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.util.ConexionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC específica para MySQL de la interfaz VisitaDAO.
 * Aplica el patrón DAO y gestiona transacciones JDBC explícitas cuando
 * corresponde.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class VisitaDAOMySQLImpl implements VisitaDAO {

    private static final String SQL_INSERT = "INSERT INTO visita (persona_id, usuario_registro_id, fecha_entrada, fecha_salida, motivo, estado, observaciones) "
            +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_BASE_SELECT = "SELECT v.id AS v_id, v.fecha_entrada, v.fecha_salida, v.motivo, v.estado, v.observaciones, "
            +
            "p.id AS p_id, p.tipo_documento, p.numero_documento, p.nombres, p.apellidos, p.email, p.telefono, p.tipo_persona, p.activo, p.fecha_registro, "
            +
            "u.id AS u_id, u.username, u.email AS u_email, u.activo AS u_activo " +
            "FROM visita v " +
            "INNER JOIN persona p ON v.persona_id = p.id " +
            "LEFT JOIN usuario u ON v.usuario_registro_id = u.id ";

    private static final String SQL_SELECT_BY_ID = SQL_BASE_SELECT + "WHERE v.id = ?";
    private static final String SQL_SELECT_ALL = SQL_BASE_SELECT + "ORDER BY v.fecha_entrada DESC";

    private static final String SQL_SELECT_ULTIMA_ACTIVA = SQL_BASE_SELECT +
            "WHERE v.persona_id = ? AND v.estado IN ('EN_CURSO', 'PENDIENTE') ORDER BY v.fecha_entrada DESC LIMIT 1";

    private static final String SQL_SELECT_ACTIVAS = SQL_BASE_SELECT +
            "WHERE v.estado = 'EN_CURSO' ORDER BY v.fecha_entrada DESC";

    private static final String SQL_SELECT_BY_RANGO_FECHAS = SQL_BASE_SELECT +
            "WHERE v.fecha_entrada BETWEEN ? AND ? ORDER BY v.fecha_entrada DESC";

    private static final String SQL_UPDATE = "UPDATE visita SET persona_id = ?, usuario_registro_id = ?, fecha_entrada = ?, fecha_salida = ?, motivo = ?, estado = ?, observaciones = ? "
            +
            "WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM visita WHERE id = ?";

    private static final String SQL_REGISTRAR_SALIDA = "UPDATE visita SET fecha_salida = ?, estado = 'FINALIZADA', observaciones = ? WHERE id = ?";

    @Override
    public Visita crear(Visita visita) {
        if (visita == null || visita.getPersona() == null)
            return null;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, visita.getPersona().getId());

            if (visita.getUsuarioRegistro() != null && visita.getUsuarioRegistro().getId() != null) {
                stmt.setLong(2, visita.getUsuarioRegistro().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            LocalDateTime fechaEntrada = visita.getFechaEntrada() != null ? visita.getFechaEntrada()
                    : LocalDateTime.now();
            stmt.setTimestamp(3, Timestamp.valueOf(fechaEntrada));

            if (visita.getFechaSalida() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setString(5, visita.getMotivo());
            stmt.setString(6, visita.getEstado() != null ? visita.getEstado() : "EN_CURSO");
            stmt.setString(7, visita.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        visita.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al insertar visita: " + e.getMessage());
        }
        return visita;
    }

    @Override
    public Optional<Visita> obtenerPorId(Long id) {
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
            System.err.println("[VisitaDAOMySQLImpl] Error al consultar visita por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Visita> listarTodos() {
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                visitas.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al listar visitas: " + e.getMessage());
        }
        return visitas;
    }

    @Override
    public boolean actualizar(Visita visita) {
        if (visita == null || visita.getId() == null)
            return false;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, visita.getPersona().getId());
            if (visita.getUsuarioRegistro() != null && visita.getUsuarioRegistro().getId() != null) {
                stmt.setLong(2, visita.getUsuarioRegistro().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }

            stmt.setTimestamp(3, Timestamp.valueOf(visita.getFechaEntrada()));
            if (visita.getFechaSalida() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setString(5, visita.getMotivo());
            stmt.setString(6, visita.getEstado());
            stmt.setString(7, visita.getObservaciones());
            stmt.setLong(8, visita.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al actualizar visita: " + e.getMessage());
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
            System.err.println("[VisitaDAOMySQLImpl] Error al eliminar visita: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long personaId) {
        if (personaId == null)
            return Optional.empty();

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ULTIMA_ACTIVA)) {

            stmt.setLong(1, personaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al buscar última visita activa: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Visita> listarVisitasActivas() {
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ACTIVAS);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                visitas.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al listar visitas activas: " + e.getMessage());
        }
        return visitas;
    }

    @Override
    public List<Visita> listarVisitasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        List<Visita> visitas = new ArrayList<>();
        if (inicio == null || fin == null)
            return visitas;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_RANGO_FECHAS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al listar por rango de fechas: " + e.getMessage());
        }
        return visitas;
    }

    @Override
    public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
        if (visitaId == null)
            return false;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_REGISTRAR_SALIDA)) {

            LocalDateTime salida = fechaSalida != null ? fechaSalida : LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(salida));
            stmt.setString(2, observaciones);
            stmt.setLong(3, visitaId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[VisitaDAOMySQLImpl] Error al registrar salida de visita: " + e.getMessage());
            return false;
        }
    }

    private Visita mapearResultSet(ResultSet rs) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getLong("v_id"));

        // Mapear Persona
        Persona p = new Persona();
        p.setId(rs.getLong("p_id"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNumeroDocumento(rs.getString("numero_documento"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setEmail(rs.getString("email"));
        p.setTelefono(rs.getString("telefono"));
        p.setTipoPersona(rs.getString("tipo_persona"));
        p.setActivo(rs.getBoolean("activo"));
        Timestamp tsP = rs.getTimestamp("fecha_registro");
        if (tsP != null)
            p.setFechaRegistro(tsP.toLocalDateTime());
        v.setPersona(p);

        // Mapear Usuario (si existe)
        long uId = rs.getLong("u_id");
        if (!rs.wasNull()) {
            Usuario u = new Usuario();
            u.setId(uId);
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("u_email"));
            u.setActivo(rs.getBoolean("u_activo"));
            v.setUsuarioRegistro(u);
        }

        // Mapear campos de Visita
        Timestamp tsEntrada = rs.getTimestamp("fecha_entrada");
        if (tsEntrada != null)
            v.setFechaEntrada(tsEntrada.toLocalDateTime());

        Timestamp tsSalida = rs.getTimestamp("fecha_salida");
        if (tsSalida != null)
            v.setFechaSalida(tsSalida.toLocalDateTime());

        v.setMotivo(rs.getString("motivo"));
        v.setEstado(rs.getString("estado"));
        v.setObservaciones(rs.getString("observaciones"));

        return v;
    }
}
