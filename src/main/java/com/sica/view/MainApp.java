package com.sica.view;

import com.sica.controller.FuncionarioController;
import com.sica.controller.GuardaController;
import com.sica.controller.LoginController;
import com.sica.controller.SupervisorController;
import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.dao.PersonaDAO;
import com.sica.dao.VisitaDAO;
import com.sica.decorator.AuditoriaVisitaDecorator;
import com.sica.exception.AccesoDenegadoException;
import com.sica.model.BitacoraAuditoria; // Importación añadida por seguridad
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.AuthService;
import com.sica.service.VisitaService;
import com.sica.service.impl.AuthServiceImpl;
import com.sica.service.impl.VisitaServiceImpl;

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
 * Aplicación Principal JavaFX (Presentation Layer - Patrón MVC & Patrones Consolidados).
 * Coordina la autenticación (LoginController), los dashboards gráficos por rol
 * (GuardaDashboardView, FuncionarioDashboardView),
 * el proxy de seguridad (SeguridadVisitaServiceProxy) y la generación de
 * reportes con Stream API (SupervisorController).
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class MainApp extends Application {

    private AuthService authService;
    private LoginController loginController;
    private VisitaService servicioVisitasBase;
    private PersonaDAO personaDAO;
    private BitacoraAuditoriaDAO bitacoraDAO;
    private Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        // Inicialización de la arquitectura de servicios y DAOs
        this.authService = new AuthServiceImpl();
        this.loginController = new LoginController(authService);

        // Repositorios en memoria para ejecución autónoma de la vista
        this.personaDAO = new PersonaDAOInMemoryLocal();
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
        stage.setResizable(true);
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
        txtUsername.setPromptText("Ej. admin@zonaacme.com");
        txtUsername.setPrefWidth(260);
        txtUsername.setStyle(
                "-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-font-size: 14px; -fx-background-radius: 5;");

        Label lblPass = new Label("Contraseña:");
        lblPass.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        lblPass.setTextFill(Color.web("#cdd6f4"));

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("••••••••");
        txtPassword.setPrefWidth(260);
        txtPassword.setStyle(
                "-fx-background-color: #313244; -fx-text-fill: #cdd6f4; -fx-font-size: 14px; -fx-background-radius: 5;");

        grid.add(lblUser, 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(lblPass, 0, 1);
        grid.add(txtPassword, 1, 1);

        // Botón de Ingreso Principal
        Button btnIngresar = new Button("INGRESAR AL SISTEMA");
        btnIngresar.setPrefWidth(280);
        btnIngresar.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;");

        // Enlace del evento con el LoginController
        btnIngresar.setOnAction(e -> procesarLogin(txtUsername.getText(), txtPassword.getText()));

        // Panel de Accesos Rápidos para Prueba de Roles (ACTUALIZADO PARA MYSQL)
        Label lblPruebas = new Label("--- Usuarios de Prueba Disponibles ---");
        lblPruebas.setTextFill(Color.web("#6c7086"));

        HBox boxBotonesPrueba = new HBox(10);
        boxBotonesPrueba.setAlignment(Pos.CENTER);

        Button btnQuickAdmin = new Button("Admin");
        btnQuickAdmin.setOnAction(e -> {
            txtUsername.setText("admin@zonaacme.com");
            txtPassword.setText("1234");
        });

        Button btnQuickGuardia = new Button("Guardia");
        btnQuickGuardia.setOnAction(e -> {
            txtUsername.setText("carlos@seguridad.com");
            txtPassword.setText("1234");
        });

        Button btnQuickFunc = new Button("Funcionario");
        btnQuickFunc.setOnAction(e -> {
            txtUsername.setText("ana@empresa.com");
            txtPassword.setText("1234");
        });

        Button btnQuickOperador = new Button("Operador (Sin Permisos)");
        btnQuickOperador.setOnAction(e -> {
            txtUsername.setText("operador@zonaacme.com");
            txtPassword.setText("1234");
        });

        boxBotonesPrueba.getChildren().addAll(btnQuickAdmin, btnQuickGuardia, btnQuickFunc, btnQuickOperador);

        root.getChildren().addAll(lblTitulo, lblSubtitulo, grid, btnIngresar, lblPruebas, boxBotonesPrueba);
        return new Scene(root, 600, 480);
    }

    /**
     * Procesa la solicitud de login capturando excepciones y desplegando los
     * dashboards por rol.
     */
    private void procesarLogin(String username, String password) {
        try {
            // Invocar el LoginController (Patrón MVC)
            Usuario usuarioLogueado = loginController.iniciarSesion(username, password);

            // Crear el Proxy de Seguridad RBAC para las operaciones del usuario
            VisitaService proxySeguridad = new SeguridadVisitaServiceProxy(servicioVisitasBase, usuarioLogueado);

            // Desplegar Ventana Principal de Dashboards por Rol
            mostrarDashboardConsolidado(usuarioLogueado, proxySeguridad);

        } catch (AccesoDenegadoException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación / RBAC",
                    "Violación de Seguridad o Credenciales Incorrectas",
                    ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado",
                    "Ocurrió una falla en el sistema",
                    ex.getMessage());
        }
    }

    /**
     * Muestra la interfaz consolidada con TabPane permitiendo navegar entre los
     * Dashboards.
     */
    private void mostrarDashboardConsolidado(Usuario usuario, VisitaService proxySeguridad) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");

        // Barra Superior
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #11111b;");

        Label lblUser = new Label("👤 Sesión Activa: " + usuario.getUsername() + " | Rol: "
                + (usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A"));
        lblUser.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblUser.setTextFill(Color.web("#89b4fa"));

        Button btnReporteStream = new Button("📊 Reporte Supervisión (Stream API)");
        btnReporteStream.setStyle("-fx-background-color: #f9e2af; -fx-text-fill: #11111b; -fx-font-weight: bold;");
        btnReporteStream.setOnAction(e -> {
            SupervisorController supervisorController = new SupervisorController(proxySeguridad);
            SupervisorController.ReporteMetricasVisitas m = supervisorController.obtenerReporteConsolidado();
            mostrarAlerta(Alert.AlertType.INFORMATION, "SICA - Reporte de Supervisión",
                    "Módulo de Reportes (Stream API & Lambdas)", m.toString());
        });

        Button btnCerrarSesion = new Button("🚪 Cerrar Sesión");
        btnCerrarSesion.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-font-weight: bold;");
        btnCerrarSesion.setOnAction(e -> {
            authService.cerrarSesion();
            primaryStage.setScene(crearEscenaLogin());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(lblUser, spacer, btnReporteStream, btnCerrarSesion);
        root.setTop(topBar);

        // Control de Pestañas (TabPane)
        TabPane tabPane = new TabPane();

        // 1. Pestaña Dashboard Guardia
        GuardaController guardaController = new GuardaController(proxySeguridad);
        GuardaDashboardView viewGuarda = new GuardaDashboardView(guardaController, personaDAO, usuario);
        Tab tabGuarda = new Tab("🛡️ Dashboard Guardia", viewGuarda);
        tabGuarda.setClosable(false);

        // 2. Pestaña Dashboard Funcionario (con Observer en Tiempo Real)
        FuncionarioController funcionarioController = new FuncionarioController(proxySeguridad);
        FuncionarioDashboardView viewFuncionario = new FuncionarioDashboardView(funcionarioController,
                usuario.getUsername(), "Gerencia TI");
        Tab tabFuncionario = new Tab("🏢 Dashboard Funcionario (Observer UI)", viewFuncionario);
        tabFuncionario.setClosable(false);

        tabPane.getTabs().addAll(tabGuarda, tabFuncionario);
        root.setCenter(tabPane);

        primaryStage.setScene(new Scene(root, 900, 680));
    }

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
        private final Map<Long, Persona> DB = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        public PersonaDAOInMemoryLocal() {
            crear(new Persona("CC", "101010", "Juan", "Pérez", "juan@correo.com", "3001112233", "VISITANTE"));
            crear(new Persona("CC", "202020", "María", "Gómez", "maria@correo.com", "3104445566", "CONTRATISTA"));
        }

        @Override
        public Persona crear(Persona p) {
            p.setId(seq.getAndIncrement());
            DB.put(p.getId(), p);
            return p;
        }

        @Override
        public Optional<Persona> obtenerPorId(Long id) {
            return Optional.ofNullable(DB.get(id));
        }

        @Override
        public List<Persona> listarTodos() {
            return new ArrayList<>(DB.values());
        }

        @Override
        public boolean actualizar(Persona p) {
            DB.put(p.getId(), p);
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return DB.remove(id) != null;
        }

        @Override
        public Optional<Persona> buscarPorDocumento(String t, String n) {
            return DB.values().stream()
                    .filter(p -> p.getTipoDocumento().equalsIgnoreCase(t) && p.getNumeroDocumento().equals(n))
                    .findFirst();
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
        private final Map<Long, Visita> DB = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(500);

        @Override
        public Visita crear(Visita v) {
            v.setId(seq.getAndIncrement());
            DB.put(v.getId(), v);
            return v;
        }

        @Override
        public Optional<Visita> obtenerPorId(Long id) {
            return Optional.ofNullable(DB.get(id));
        }

        @Override
        public List<Visita> listarTodos() {
            return new ArrayList<>(DB.values());
        }

        @Override
        public boolean actualizar(Visita v) {
            DB.put(v.getId(), v);
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return DB.remove(id) != null;
        }

        @Override
        public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long pId) {
            return DB.values().stream().filter(v -> v.getPersona() != null && pId.equals(v.getPersona().getId()))
                    .reduce((f, s) -> s);
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
            Visita v = DB.get(vId);
            if (v != null) {
                v.setEstado("FINALIZADA");
                return true;
            }
            return false;
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