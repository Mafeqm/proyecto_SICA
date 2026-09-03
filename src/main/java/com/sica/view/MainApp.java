package com.sica.view;

import com.sica.controller.GuardaController;
import com.sica.controller.LoginController;
import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.dao.PersonaDAO;
import com.sica.dao.VisitaDAO;
import com.sica.decorator.AuditoriaVisitaDecorator;
import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.AuthService;
import com.sica.service.VisitaService;
import com.sica.service.impl.AuthServiceImpl;
import com.sica.service.impl.VisitaServiceImpl;
import com.sica.strategy.AccesoPreRegistrado;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aplicación Principal JavaFX (Presentation Layer - Patrón MVC).
 * Proporciona la ventana gráfica de Login construida en código Java nativo (sin
 * FXML),
 * enlaza el controlador LoginController y muestra la gestión de alertas (Alert)
 * ante
 * situaciones de éxito o excepciones de seguridad RBAC
 * (AccesoDenegadoException).
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class MainApp extends Application {

    private AuthService authService;
    private LoginController loginController;
    private VisitaService servicioVisitasBase;
    private BitacoraAuditoriaDAO bitacoraDAO;
    private Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        // Inicialización de la arquitectura de servicios y DAOs
        this.authService = new AuthServiceImpl();
        this.loginController = new LoginController(authService);

        // Repositorios en memoria para ejecución autónoma de la vista
        PersonaDAO personaDAO = new PersonaDAOInMemoryLocal();
        VisitaDAO visitaDAO = new VisitaDAOInMemoryLocal();
        this.bitacoraDAO = new BitacoraDAOInMemoryLocal();

        VisitaService base = new VisitaServiceImpl(visitaDAO);
        this.servicioVisitasBase = new AuditoriaVisitaDecorator(base, bitacoraDAO);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("SICA - Sistema Integrado de Control de Acceso");

        // Construir la escena de Login
        Scene loginScene = crearEscenaLogin();
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Construye la interfaz gráfica de Login utilizando Layouts nativos de JavaFX.
     */
    private Scene crearEscenaLogin() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1e2e;"); // Estilo Cyberpunk / Dark Mode

        // Título del Sistema
        Label lblTitulo = new Label("SICA");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        lblTitulo.setTextFill(Color.web("#89b4fa"));

        Label lblSubtitulo = new Label("Control de Acceso Integrado & RBAC");
        lblSubtitulo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        lblSubtitulo.setTextFill(Color.web("#a6adc8"));

        // Formulario de Credenciales (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Label lblUser = new Label("Usuario:");
        lblUser.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        lblUser.setTextFill(Color.web("#cdd6f4"));

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Ej. admin, guardia, operador");
        txtUsername.setPrefWidth(240);
        txtUsername.setStyle(
                "-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-font-size: 14px; -fx-background-radius: 5;");

        Label lblPass = new Label("Contraseña:");
        lblPass.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        lblPass.setTextFill(Color.web("#cdd6f4"));

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("••••••••");
        txtPassword.setPrefWidth(240);
        txtPassword.setStyle(
                "-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-font-size: 14px; -fx-background-radius: 5;");

        grid.add(lblUser, 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(lblPass, 0, 1);
        grid.add(txtPassword, 1, 1);

        // Botón de Ingreso Principal
        Button btnIngresar = new Button("INGRESAR AL SISTEMA");
        btnIngresar.setPrefWidth(260);
        btnIngresar.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;");

        // Enlace del evento con el LoginController
        btnIngresar.setOnAction(e -> procesarLogin(txtUsername.getText(), txtPassword.getText()));

        // Panel de Accesos Rápidos para Prueba de Roles
        Label lblPruebas = new Label("--- Usuarios de Prueba Disponibles ---");
        lblPruebas.setTextFill(Color.web("#6c7086"));

        HBox boxBotonesPrueba = new HBox(10);
        boxBotonesPrueba.setAlignment(Pos.CENTER);

        Button btnQuickAdmin = new Button("Admin (Total)");
        btnQuickAdmin.setOnAction(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
        });

        Button btnQuickGuardia = new Button("Guardia (Accesos)");
        btnQuickGuardia.setOnAction(e -> {
            txtUsername.setText("guardia");
            txtPassword.setText("guardia123");
        });

        Button btnQuickSinPermisos = new Button("Operador (Sin Permisos)");
        btnQuickSinPermisos.setOnAction(e -> {
            txtUsername.setText("operador");
            txtPassword.setText("operador123");
        });

        boxBotonesPrueba.getChildren().addAll(btnQuickAdmin, btnQuickGuardia, btnQuickSinPermisos);

        root.getChildren().addAll(lblTitulo, lblSubtitulo, grid, btnIngresar, lblPruebas, boxBotonesPrueba);
        return new Scene(root, 520, 440);
    }

    /**
     * Procesa la solicitud de login capturando excepciones y mostrando
     * retroalimentación en Alertas JavaFX.
     */
    private void procesarLogin(String username, String password) {
        try {
            // Invocar el LoginController (Patrón MVC)
            Usuario usuarioLogueado = loginController.iniciarSesion(username, password);

            // Crear el Proxy de Seguridad RBAC para las operaciones del usuario
            VisitaService proxySeguridad = new SeguridadVisitaServiceProxy(servicioVisitasBase, usuarioLogueado);

            // Notificar éxito mediante Alert JavaFX
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sesión Iniciada",
                    "¡Bienvenido a SICA, " + usuarioLogueado.getUsername() + "!",
                    "Rol Asignado: "
                            + (usuarioLogueado.getRol() != null ? usuarioLogueado.getRol().getNombre() : "Sin Rol") +
                            "\nEstado de Cuenta: ACTIVA");

            // Demostrar prueba de permiso RBAC con el Proxy
            probarOperacionProxyGuardia(proxySeguridad, usuarioLogueado);

        } catch (AccesoDenegadoException ex) {
            // Capturar violación de seguridad RBAC o credenciales y mostrar Alert ERROR
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación / RBAC",
                    "Violación de Seguridad o Credenciales Incortectas",
                    ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado",
                    "Ocurrió una falla en el sistema",
                    ex.getMessage());
        }
    }

    /**
     * Prueba la ejecución de una acción a través del GuardaController y Proxy RBAC.
     */
    private void probarOperacionProxyGuardia(VisitaService proxySeguridad, Usuario usuario) {
        GuardaController guardaController = new GuardaController(proxySeguridad);
        Persona persona = new Persona("CC", "12345678", "Visitante", "Prueba", "v@test.com", "555-1234", "VISITANTE");
        persona.setId(99L);

        try {
            System.out
                    .println("[MainApp JavaFX] Intentando registrar acceso mediante GuardaController + Proxy RBAC...");
            Visita v = guardaController.registrarIngreso(persona, new AccesoPreRegistrado(), usuario, "Prueba JavaFX",
                    "Ingreso exitoso");

            mostrarAlerta(Alert.AlertType.INFORMATION, "Acción Autorizada (Proxy RBAC)",
                    "Operación Registrada Exitosamente",
                    "Visita ID #" + v.getId() + " registrada por el usuario '" + usuario.getUsername()
                            + "' [Permiso 'VISITA_REGISTRAR' VALIDADO CON ÉXITO].");

        } catch (AccesoDenegadoException e) {
            // Se dispara si el usuario autenticado (ej. 'operador') no tiene el permiso
            // 'VISITA_REGISTRAR'
            mostrarAlerta(Alert.AlertType.WARNING, "Acceso Denegado (Proxy RBAC)",
                    "Operación Bloqueada por el Proxy de Seguridad",
                    e.getMessage());
        }
    }

    /**
     * Muestra una ventana emergente de alerta (JavaFX Alert) para dar
     * retroalimentación visual al usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(contenido);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // DAOs auxiliares en memoria para ejecución gráfica independiente
    static class PersonaDAOInMemoryLocal implements PersonaDAO {
        @Override
        public Persona crear(Persona p) {
            p.setId(1L);
            return p;
        }

        @Override
        public Optional<Persona> obtenerPorId(Long id) {
            return Optional.empty();
        }

        @Override
        public List<Persona> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public boolean actualizar(Persona p) {
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return true;
        }

        @Override
        public Optional<Persona> buscarPorDocumento(String t, String n) {
            return Optional.empty();
        }

        @Override
        public List<Persona> listarPorTipo(String tipoPersona) {
            return new ArrayList<>();
        }

        @Override
        public boolean cambiarEstadoActivo(Long id, boolean activo) {
            return true;
        }
    }

    static class VisitaDAOInMemoryLocal implements VisitaDAO {
        private final AtomicLong seq = new AtomicLong(500);

        @Override
        public Visita crear(Visita v) {
            v.setId(seq.getAndIncrement());
            return v;
        }

        @Override
        public Optional<Visita> obtenerPorId(Long id) {
            return Optional.empty();
        }

        @Override
        public List<Visita> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public boolean actualizar(Visita v) {
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return true;
        }

        @Override
        public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long pId) {
            return Optional.empty();
        }

        @Override
        public List<Visita> listarVisitasActivas() {
            return new ArrayList<>();
        }

        @Override
        public List<Visita> listarVisitasPorRangoFechas(java.time.LocalDateTime i, java.time.LocalDateTime f) {
            return new ArrayList<>();
        }

        @Override
        public boolean registrarSalida(Long vId, java.time.LocalDateTime fS, String obs) {
            return true;
        }
    }

    static class BitacoraDAOInMemoryLocal implements BitacoraAuditoriaDAO {
        @Override
        public BitacoraAuditoria registrar(BitacoraAuditoria b) {
            b.setId(1L);
            return b;
        }

        @Override
        public List<BitacoraAuditoria> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public List<BitacoraAuditoria> listarPorUsuario(Long uId) {
            return new ArrayList<>();
        }
    }
}
