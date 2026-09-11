package com.sica.view;

import com.sica.controller.FuncionarioController;
import com.sica.model.Visita;
import com.sica.observer.NotificadorVisitaSubject;
import com.sica.observer.ObserverFuncionario;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Dashboard de Presentación JavaFX para Funcionarios de la Empresa (Patrón MVC
 * + Patrón Observer).
 * Muestra un TableView de JavaFX con las visitas pendientes de autorización.
 * Implementa la interfaz ObserverFuncionario para recibir notificaciones en
 * tiempo real
 * cuando ingresa una visita no anunciada, actualizando la GUI de forma segura
 * mediante Platform.runLater().
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class FuncionarioDashboardView extends VBox implements ObserverFuncionario {

    private final FuncionarioController funcionarioController;
    private final String nombreFuncionario;
    private final String departamento;
    private com.sica.model.Usuario usuarioLogueado;

    private TableView<Visita> tblVisitas;
    private ObservableList<Visita> listaVisitasObservable;
    private Label lblEstadoObserver;

    public FuncionarioDashboardView(FuncionarioController funcionarioController, String nombreFuncionario,
            String departamento) {
        this(funcionarioController, nombreFuncionario, departamento, null);
    }

    public FuncionarioDashboardView(FuncionarioController funcionarioController, String nombreFuncionario,
            String departamento, com.sica.model.Usuario usuarioLogueado) {
        this.funcionarioController = funcionarioController;
        this.nombreFuncionario = nombreFuncionario;
        this.departamento = departamento;
        this.usuarioLogueado = usuarioLogueado;

        inicializarComponentes();

        // Suscribir este Dashboard al Notificador de Visitas (Patrón Observer)
        NotificadorVisitaSubject.getInstanciaGlobal().registrarObserver(this);
    }

    private void inicializarComponentes() {
        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #1e1e2e;");

        // Encabezado
        Label lblTitulo = new Label("SICA - Panel de Aprobación de Funcionarios");
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#89b4fa"));

        Label lblSubtitulo = new Label("Funcionario: " + nombreFuncionario + " | Área: " + departamento);
        lblSubtitulo.setTextFill(Color.web("#a6adc8"));

        // Indicador de Notificaciones Observer en Tiempo Real
        lblEstadoObserver = new Label("🟢 Observer Activo: Escuchando notificaciones de visitantes no anunciados...");
        lblEstadoObserver.setStyle(
                "-fx-background-color: #313244; -fx-padding: 8; -fx-text-fill: #a6e3a1; -fx-font-weight: bold; -fx-background-radius: 5;");

        // TableView de Visitas Pendientes
        tblVisitas = new TableView<>();
        tblVisitas.setPrefHeight(280);
        tblVisitas.setStyle("-fx-background-color: #313244; -fx-text-fill: #cdd6f4;");

        TableColumn<Visita, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getId() != null ? cell.getValue().getId().toString() : "-"));
        colId.setPrefWidth(60);

        TableColumn<Visita, String> colPersona = new TableColumn<>("Visitante");
        colPersona.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getPersona() != null ? cell.getValue().getPersona().getNombreCompleto() : "N/A"));
        colPersona.setPrefWidth(180);

        TableColumn<Visita, String> colDoc = new TableColumn<>("Documento");
        colDoc.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getPersona() != null ? cell.getValue().getPersona().getNumeroDocumento() : "-"));
        colDoc.setPrefWidth(120);

        TableColumn<Visita, String> colMotivo = new TableColumn<>("Motivo");
        colMotivo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMotivo()));
        colMotivo.setPrefWidth(200);

        TableColumn<Visita, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEstado()));
        colEstado.setPrefWidth(110);

        tblVisitas.getColumns().addAll(colId, colPersona, colDoc, colMotivo, colEstado);

        listaVisitasObservable = FXCollections.observableArrayList();
        tblVisitas.setItems(listaVisitasObservable);

        // Botones de Acción
        HBox boxBotones = new HBox(15);
        boxBotones.setAlignment(Pos.CENTER_LEFT);

        Button btnAprobar = new Button("✔️ APROBAR ACCESO");
        btnAprobar.setStyle(
                "-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnAprobar.setOnAction(e -> aprobarVisitaSeleccionada());

        Button btnRechazar = new Button("✖️ RECHAZAR ACCESO");
        btnRechazar.setStyle(
                "-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnRechazar.setOnAction(e -> rechazarVisitaSeleccionada());

        Button btnRefrescar = new Button("🔄 Refrescar Tabla");
        btnRefrescar.setOnAction(e -> cargarVisitasPendientes());

        Button btnPersonalPresente = new Button("🏢 Ver Personal Presente en el Complejo");
        btnPersonalPresente.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnPersonalPresente.setOnAction(e -> mostrarVentanaPersonalPresente());

        boxBotones.getChildren().addAll(btnAprobar, btnRechazar, btnRefrescar, btnPersonalPresente);

        this.getChildren().addAll(lblTitulo, lblSubtitulo, lblEstadoObserver, tblVisitas, boxBotones);

        // Cargar datos iniciales
        cargarVisitasPendientes();
    }

    private void mostrarVentanaPersonalPresente() {
        FuncionarioConsolaView consolaView = new FuncionarioConsolaView(funcionarioController);
        String reporte = consolaView.mostrarPersonalPresenteEnConsola(usuarioLogueado);

        List<Visita> presentes = funcionarioController.obtenerPersonalPresenteEnComplejo(usuarioLogueado);
        if (presentes.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Personal Presente en el Complejo",
                    "No hay ninguna persona de su empresa actualmente dentro del complejo.");
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Personal Presente en el Complejo",
                    "Se encontraron " + presentes.size() + " personas de su empresa dentro del complejo:\n\n" + reporte);
        }
    }

    public void cargarVisitasPendientes() {
        List<Visita> pendientes = funcionarioController.listarVisitasPendientes();
        listaVisitasObservable.setAll(pendientes);
    }

    private void aprobarVisitaSeleccionada() {
        Visita seleccionada = tblVisitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Por favor seleccione una visita de la tabla.");
            return;
        }

        boolean ok = funcionarioController.aprobarVisita(seleccionada.getId(),
                "Aprobada desde Dashboard de Funcionario");
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Aprobación Exitosa",
                    "La visita ID #" + seleccionada.getId() + " ha sido APROBADA e ingresó en estado EN_CURSO.");
            cargarVisitasPendientes();
        }
    }

    private void rechazarVisitaSeleccionada() {
        Visita seleccionada = tblVisitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Por favor seleccione una visita de la tabla.");
            return;
        }

        seleccionada.setEstado("RECHAZADA");
        mostrarAlerta(Alert.AlertType.INFORMATION, "Acceso Rechazado",
                "La visita ID #" + seleccionada.getId() + " ha sido RECHAZADA.");
        cargarVisitasPendientes();
    }

    /**
     * IMPLEMENACIÓN DEL PATRÓN OBSERVER EN UI.
     * Método invocado por NotificadorVisitaSubject cuando se registra una nueva
     * visita no anunciada.
     * Garantiza la actualización segura de la UI mediante Platform.runLater().
     */
    @Override
    public void onNuevaVisitaPendiente(Visita visita) {
        // Ejecución en el Hilo Gráfico de JavaFX (JavaFX Application Thread)
        Platform.runLater(() -> {
            System.out.println("[UI OBSERVER] 🔔 Notificación recibida en tiempo real para: "
                    + (visita.getPersona() != null ? visita.getPersona().getNombreCompleto() : "Visitante"));
            lblEstadoObserver.setText("🔔 ¡NUEVA VISITA NO ANUNCIADA! Revisa la tabla para aprobar el ingreso.");
            lblEstadoObserver.setStyle(
                    "-fx-background-color: #fab387; -fx-padding: 8; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-background-radius: 5;");

            // Refrescar automáticamente la tabla con el nuevo elemento
            cargarVisitasPendientes();

            // Notificación gráfica emergente
            Alert alertNotif = new Alert(Alert.AlertType.INFORMATION);
            alertNotif.setTitle("Notificación Observer en Tiempo Real");
            alertNotif.setHeaderText("⚡ Alerta de Ingreso No Anunciado");
            alertNotif.setContentText(
                    "El visitante " + (visita.getPersona() != null ? visita.getPersona().getNombreCompleto() : "")
                            + " ha llegado a recepción y requiere su autorización inmediata.");
            alertNotif.show();
        });
    }

    @Override
    public String getNombreFuncionario() {
        return nombreFuncionario;
    }

    @Override
    public String getDepartamento() {
        return departamento;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
