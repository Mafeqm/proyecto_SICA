package com.sica.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria encargada de la gestión de la conexión a la Base de Datos.
 * Aplica el patrón de diseño Singleton para garantizar una única instancia de gestión
 * de conexión activa dentro del ciclo de vida de la aplicación.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility Principle (SRP): Responsabilidad única de proveer y administrar la conexión JDBC.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class ConexionDB {

    // Instancia única de la clase (Singleton)
    private static ConexionDB instancia;

    // Conexión JDBC activa
    private Connection conexion;

    // Configuración predeterminada de la base de datos (se puede parametrizar mediante variables de entorno o archivo de propiedades)
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/sica_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "sica_user";
    private static final String DEFAULT_PASSWORD = "Sica2026*";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private String url;
    private String user;
    private String password;
    private String driver;

    /**
     * Constructor privado para prevenir la instanciación directa desde fuera de la clase.
     * Carga el driver JDBC e inicializa las credenciales por defecto.
     */
    private ConexionDB() {
        this.url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DEFAULT_URL;
        this.user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
        this.password = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : DEFAULT_PASSWORD;
        this.driver = System.getenv("DB_DRIVER") != null ? System.getenv("DB_DRIVER") : DEFAULT_DRIVER;

        cargarDriver();
    }

    /**
     * Carga de manera dinámica el driver de JDBC configurado.
     */
    private void cargarDriver() {
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException e) {
            System.err.println("[ConexionDB] Advertencia: Driver JDBC no encontrado en el classpath ('" + driver 
                    + "'). Asegúrate de incluir el conector JDBC correspondiente. Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene la instancia única de ConexionDB (Thread-Safe con Double-Checked Locking).
     * 
     * @return Instancia única de ConexionDB.
     */
    public static ConexionDB getInstance() {
        if (instancia == null) {
            synchronized (ConexionDB.class) {
                if (instancia == null) {
                    instancia = new ConexionDB();
                }
            }
        }
        return instancia;
    }

    /**
     * Obtiene una conexión activa a la base de datos.
     * Si la conexión no existe o está cerrada, intenta abrir una nueva conexión.
     * 
     * @return Objeto Connection activo de java.sql.
     * @throws SQLException Si ocurre un error al intentar conectarse a la BD.
     */
    public Connection getConnection() throws SQLException {
        if (this.conexion == null || this.conexion.isClosed()) {
            try {
                this.conexion = DriverManager.getConnection(this.url, this.user, this.password);
                System.out.println("[ConexionDB] Conexión establecida exitosamente con la base de datos.");
            } catch (SQLException e) {
                System.err.println("[ConexionDB] Error al conectar con la base de datos: " + e.getMessage());
                throw e;
            }
        }
        return this.conexion;
    }

    /**
     * Cierra de manera segura la conexión actual si se encuentra abierta.
     */
    public void cerrarConexion() {
        if (this.conexion != null) {
            try {
                if (!this.conexion.isClosed()) {
                    this.conexion.close();
                    System.out.println("[ConexionDB] Conexión a la base de datos cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[ConexionDB] Error al intentar cerrar la conexión: " + e.getMessage());
            } finally {
                this.conexion = null;
            }
        }
    }

    /**
     * Permite reconfigurar las credenciales dinámicamente si es necesario.
     * 
     * @param url URL JDBC de la base de datos.
     * @param user Usuario de acceso.
     * @param password Contraseña de acceso.
     * @param driver Nombre completo de la clase del driver JDBC.
     */
    public void reconfigurar(String url, String user, String password, String driver) {
        cerrarConexion();
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        cargarDriver();
    }
}
