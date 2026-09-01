package com.sica.dao.impl;

import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.model.BitacoraAuditoria;
import com.sica.model.Usuario;
import com.sica.util.ConexionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC específica para MySQL de la interfaz
 * BitacoraAuditoriaDAO.
 * Aplica el patrón DAO y gestión segura de recursos mediante try-with-resources
 * y PreparedStatement.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class BitacoraAuditoriaDAOMySQLImpl implements BitacoraAuditoriaDAO {

    private static final String SQL_INSERT = "INSERT INTO bitacora_auditoria (usuario_id, accion, tabla_afectada, registro_id, detalle, fecha_hora, direccion_ip) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL = "SELECT id, usuario_id, accion, tabla_afectada, registro_id, detalle, fecha_hora, direccion_ip FROM bitacora_auditoria ORDER BY fecha_hora DESC";
    private static final String SQL_SELECT_BY_USUARIO = "SELECT id, usuario_id, accion, tabla_afectada, registro_id, detalle, fecha_hora, direccion_ip FROM bitacora_auditoria WHERE usuario_id = ? ORDER BY fecha_hora DESC";

    @Override
    public BitacoraAuditoria registrar(BitacoraAuditoria bitacora) {
        if (bitacora == null)
            return null;

        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            if (bitacora.getUsuario() != null && bitacora.getUsuario().getId() != null) {
                stmt.setLong(1, bitacora.getUsuario().getId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }

            stmt.setString(2, bitacora.getAccion());
            stmt.setString(3, bitacora.getTablaAfectada());

            if (bitacora.getRegistroId() != null) {
                stmt.setLong(4, bitacora.getRegistroId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setString(5, bitacora.getDetalle());
            stmt.setTimestamp(6,
                    Timestamp.valueOf(bitacora.getFechaHora() != null ? bitacora.getFechaHora() : LocalDateTime.now()));
            stmt.setString(7, bitacora.getDireccionIP() != null ? bitacora.getDireccionIP() : "127.0.0.1");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bitacora.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BitacoraAuditoriaDAOMySQLImpl] Error al registrar evento de auditoría en MySQL: "
                    + e.getMessage());
        }
        return bitacora;
    }

    @Override
    public List<BitacoraAuditoria> listarTodos() {
        List<BitacoraAuditoria> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BitacoraAuditoriaDAOMySQLImpl] Error al listar auditoría: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<BitacoraAuditoria> listarPorUsuario(Long usuarioId) {
        List<BitacoraAuditoria> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_USUARIO)) {

            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(
                    "[BitacoraAuditoriaDAOMySQLImpl] Error al listar auditoría por usuario: " + e.getMessage());
        }
        return lista;
    }

    private BitacoraAuditoria mapearResultSet(ResultSet rs) throws SQLException {
        BitacoraAuditoria b = new BitacoraAuditoria();
        b.setId(rs.getLong("id"));

        long uId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            Usuario u = new Usuario();
            u.setId(uId);
            b.setUsuario(u);
        }

        b.setAccion(rs.getString("accion"));
        b.setTablaAfectada(rs.getString("tabla_afectada"));

        long rId = rs.getLong("registro_id");
        if (!rs.wasNull()) {
            b.setRegistroId(rId);
        }

        b.setDetalle(rs.getString("detalle"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        if (ts != null) {
            b.setFechaHora(ts.toLocalDateTime());
        }
        b.setDireccionIP(rs.getString("direccion_ip"));
        return b;
    }
}
