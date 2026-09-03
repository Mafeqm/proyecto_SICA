package com.sica.view;

import com.sica.controller.GuardaController;
import com.sica.dao.PersonaDAO;
import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.strategy.AccesoNoAnunciado;
import com.sica.strategy.AccesoPreRegistrado;
import com.sica.strategy.AccesoSalidaOlvidada;
import com.sica.strategy.EstrategiaAcceso;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Dashboard de Presentación JavaFX para Guardias de Seguridad (Patrón MVC).
 * Permite la búsqueda de personas por documento de identidad, visualización de
 * datos y foto,
 * selección del flujo de ingreso (Patrón Strategy) y registro de entradas y
 * salidas
 * interactuando estrictamente a través de GuardaController y la protección del
 * Proxy RBAC.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class GuardaDashboardView extends VBox {

    private final GuardaController guardaController;
    private final PersonaDAO personaDAO;
    private final Usuario usuarioGuardia;

    // Componentes de Búsqueda
    private ComboBox<String> cbTipoDoc;
    private TextField txtNumDoc;
    private Label lblNombrePersona;
    private Label lblDocPersona;
    private Label lblTipoPersona;
    private Label lblFotoSimulada;
    private Persona personaEncontrada;

    // Componentes de Registro de Acceso
    private ComboBox<String> cbEstrategia;
    private TextField txtMotivo;
    private TextField txtObservaciones;
    private TextField txtVisitaIdSalida;

    public GuardaDashboardView(GuardaController guardaController, PersonaDAO personaDAO, Usuario usuarioGuardia) {
        this.guardaController = guardaController;
        this.personaDAO = personaDAO;
        this.usuarioGuardia = usuarioGuardia;

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #1e1e2e;");

        // Encabezado
        Label lblHeader = new Label("SICA - Panel del Guardia de Seguridad");
        lblHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblHeader.setTextFill(Color.web("#89b4fa"));

        Label lblGuardia = new Label(
                "Guardia Responsable: " + (usuarioGuardia != null ? usuarioGuardia.getUsername() : "N/A"));
        lblGuardia.setTextFill(Color.web("#a6adc8"));

        // 1. Panel de Búsqueda de Persona
        TitledPane paneBusqueda = crearPaneBusqueda();

        // 2. Panel de Acciones de Acceso (Strategy)
        TitledPane paneAcciones = crearPaneAcciones();

        this.getChildren().addAll(lblHeader, lblGuardia, paneBusqueda, paneAcciones);
    }

    private TitledPane crearPaneBusqueda() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: #313244;");

        HBox hboxFiltros = new HBox(10);
        hboxFiltros.setAlignment(Pos.CENTER_LEFT);

        cbTipoDoc = new ComboBox<>();
        cbTipoDoc.getItems().addAll("CC", "CE", "PASAPORTE", "PEP");
        cbTipoDoc.setValue("CC");

        txtNumDoc = new TextField();
        txtNumDoc.setPromptText("Número de documento");
        txtNumDoc.setPrefWidth(180);

        Button btnBuscar = new Button("🔍 Buscar Persona");
        btnBuscar.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold;");
        btnBuscar.setOnAction(e -> buscarPersona());

        hboxFiltros.getChildren().addAll(new Label("Tipo:"), cbTipoDoc, txtNumDoc, btnBuscar);

        // Tarjeta de Datos del Visitante Cargado
        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(15);
        gridInfo.setVgap(8);

        lblNombrePersona = new Label("[No seleccionado]");
        lblNombrePersona.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblNombrePersona.setTextFill(Color.web("#a6e3a1"));

        lblDocPersona = new Label("-");
        lblDocPersona.setTextFill(Color.web("#cdd6f4"));

        lblTipoPersona = new Label("-");
        lblTipoPersona.setTextFill(Color.web("#cdd6f4"));

        lblFotoSimulada = new Label("📷 [Esperando foto...]");
        lblFotoSimulada.setStyle(
                "-fx-background-color: #45475a; -fx-padding: 8; -fx-text-fill: #f9e2af; -fx-background-radius: 5;");

        gridInfo.add(new Label("Nombre:"), 0, 0);
        gridInfo.add(lblNombrePersona, 1, 0);
        gridInfo.add(new Label("Documento:"), 0, 1);
        gridInfo.add(lblDocPersona, 1, 1);
        gridInfo.add(new Label("Tipo Perfil:"), 0, 2);
        gridInfo.add(lblTipoPersona, 1, 2);
        gridInfo.add(new Label("Foto/Avatar:"), 0, 3);
        gridInfo.add(lblFotoSimulada, 1, 3);

        vbox.getChildren().addAll(hboxFiltros, new Separator(), gridInfo);
        TitledPane pane = new TitledPane("1. Búsqueda y Verificación de Visitante", vbox);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane crearPaneAcciones() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: #313244;");

        cbEstrategia = new ComboBox<>();
        cbEstrategia.getItems().addAll(
                "ACCESO_PRE_REGISTRADO (Cita / Autorizado)",
                "ACCESO_NO_ANUNCIADO (Notifica a Funcionario)",
                "ACCESO_SALIDA_OLVIDADA (Reingreso con Cierre Automático)");
        cbEstrategia.setValue("ACCESO_PRE_REGISTRADO (Cita / Autorizado)");
        cbEstrategia.setPrefWidth(320);

        txtMotivo = new TextField();
        txtMotivo.setPromptText("Ej. Reunión con Gerencia TI / Entrega de suministros");

        txtObservaciones = new TextField();
        txtObservaciones.setPromptText("Ej. Portando carné temporal #42");

        Button btnIngresar = new Button("📥 REGISTRAR INGRESO");
        btnIngresar.setStyle(
                "-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnIngresar.setOnAction(e -> ejecutarIngreso());

        // Sección Salida
        txtVisitaIdSalida = new TextField();
        txtVisitaIdSalida.setPromptText("ID Visita para Salida");
        txtVisitaIdSalida.setPrefWidth(140);

        Button btnSalida = new Button("📤 REGISTRAR SALIDA");
        btnSalida.setStyle(
                "-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnSalida.setOnAction(e -> ejecutarSalida());

        grid.add(new Label("Flujo de Ingreso (Strategy):"), 0, 0);
        grid.add(cbEstrategia, 1, 0);
        grid.add(new Label("Motivo Visita:"), 0, 1);
        grid.add(txtMotivo, 1, 1);
        grid.add(new Label("Observaciones:"), 0, 2);
        grid.add(txtObservaciones, 1, 2);
        grid.add(btnIngresar, 1, 3);

        grid.add(new Separator(), 0, 4, 2, 1);
        grid.add(new Label("Registrar Egreso (Visita ID):"), 0, 5);
        HBox boxSalida = new HBox(10, txtVisitaIdSalida, btnSalida);
        grid.add(boxSalida, 1, 5);

        TitledPane pane = new TitledPane("2. Operaciones de Control de Acceso (Patrón Strategy & Proxy RBAC)", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private void buscarPersona() {
        String tDoc = cbTipoDoc.getValue();
        String nDoc = txtNumDoc.getText() != null ? txtNumDoc.getText().trim() : "";

        if (nDoc.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Búsqueda", "Ingrese un número de documento para buscar.");
            return;
        }

        Optional<Persona> opt = personaDAO.buscarPorDocumento(tDoc, nDoc);
        if (opt.isPresent()) {
            this.personaEncontrada = opt.get();
            lblNombrePersona.setText(personaEncontrada.getNombreCompleto());
            lblDocPersona
                    .setText(personaEncontrada.getTipoDocumento() + " - " + personaEncontrada.getNumeroDocumento());
            lblTipoPersona.setText(personaEncontrada.getTipoPersona());

            String fotoUrl = personaEncontrada.getUrlFoto() != null ? personaEncontrada.getUrlFoto()
                    : "/assets/photos/" + nDoc + ".png";
            lblFotoSimulada.setText("🖼️ Foto Cargada: " + fotoUrl);
        } else {
            // Crear persona temporal para agilizar pruebas
            this.personaEncontrada = personaDAO.crear(new Persona(tDoc, nDoc, "Visitante", "Doc-" + nDoc,
                    nDoc + "@correo.com", "3000000000", "VISITANTE"));
            lblNombrePersona.setText(personaEncontrada.getNombreCompleto());
            lblDocPersona
                    .setText(personaEncontrada.getTipoDocumento() + " - " + personaEncontrada.getNumeroDocumento());
            lblTipoPersona.setText(personaEncontrada.getTipoPersona() + " [NUEVO REGISTRO]");
            lblFotoSimulada.setText("🖼️ Foto Generada: /assets/photos/avatar_default.png");
        }
    }

    private void ejecutarIngreso() {
        if (personaEncontrada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Primero debe buscar o seleccionar una persona.");
            return;
        }

        String opcionStrategy = cbEstrategia.getValue();
        EstrategiaAcceso estrategia;

        if (opcionStrategy.startsWith("ACCESO_NO_ANUNCIADO")) {
            estrategia = new AccesoNoAnunciado();
        } else if (opcionStrategy.startsWith("ACCESO_SALIDA_OLVIDADA")) {
            estrategia = new AccesoSalidaOlvidada();
        } else {
            estrategia = new AccesoPreRegistrado();
        }

        try {
            Visita v = guardaController.registrarIngreso(
                    personaEncontrada,
                    estrategia,
                    usuarioGuardia,
                    txtMotivo.getText(),
                    txtObservaciones.getText());

            mostrarAlerta(Alert.AlertType.INFORMATION, "Ingreso Registrado",
                    "Visita Creada Exitosamente",
                    "Visita ID #" + v.getId() + " | Estado: " + v.getEstado() + " | Estrategia: "
                            + estrategia.getNombreEstrategia());

        } catch (AccesoDenegadoException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado (Proxy RBAC)", "Permiso Insuficiente",
                    e.getMessage());
        }
    }

    private void ejecutarSalida() {
        String idStr = txtVisitaIdSalida.getText() != null ? txtVisitaIdSalida.getText().trim() : "";
        if (idStr.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Salida", "Ingrese el ID de la visita a cerrar.");
            return;
        }

        try {
            Long vId = Long.parseLong(idStr);
            boolean exito = guardaController.registrarSalida(vId, "Salida de instalaciones registrada por guardia");

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Salida Registrada", "Egreso Exitoso",
                        "La visita ID #" + vId + " ha sido finalizada correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Salida", "No se encontró la visita activa con el ID indicado.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "El ID de la visita debe ser numérico.");
        } catch (AccesoDenegadoException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado (Proxy RBAC)", "Permiso Insuficiente",
                    e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        mostrarAlerta(tipo, titulo, null, mensaje);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecera, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
