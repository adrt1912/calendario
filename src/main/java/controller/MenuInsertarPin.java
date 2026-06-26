package controller;

import model.ConexionBD;
import model.GestorTareas;
import View.view;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MenuInsertarPin {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField boxUsuarios;

    @FXML
    private PasswordField campoContrasenia;

    @FXML
    private Text err;

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    @FXML
    private void comprobarPIN() {
        String usuario = boxUsuarios.getText();
        String pin = campoContrasenia.getText();
        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();

        if (usuario == null || usuario.isBlank() || pin == null || pin.isBlank()) {
            err.setText(bundle.getString("establecerPin.error.vacio"));
            return;
        }

        int idObtenido = ConexionBD.getConexionBD().verificarUsuarioYObtenerId(usuario, pin);

        if (idObtenido != -1) {
            GestorTareas.getGestorTareas().setIdUsuarioLogueado(idObtenido);

            // 1. CARGA ORDENADA: Primero preferencias (Idiomas/Tema) y luego sus tareas
            ConexionBD.getConexionBD().cargarDatosUsuario(idObtenido);
            ConexionBD.getConexionBD().cargarDatosDeBD(idObtenido);

            // 2. Saltamos al calendario en el hilo gráfico
            Platform.runLater(() -> {
                try {
                    Stage stagePrincipal = view.getPrimaryStage();
                    stagePrincipal.setOnCloseRequest(event -> {
                        event.consume();
                        stagePrincipal.hide();
                    });

                    // Renderiza la vista leyendo el nuevo idioma cargado
                    view.showInitialView();
                    GestorTareas.getGestorTareas().verificarTareasHoy();
                } catch (Exception e) {
                    err.setText("Error crítico al cargar el menú principal");
                }
            });
        } else {
            campoContrasenia.clear();
            campoContrasenia.requestFocus();
            err.setText("Usuario o PIN incorrectos. Inténtalo de nuevo.");
        }
    }

    @FXML
    private void entrarComoInvitado(){
        ConexionBD.getConexionBD().registrarNuevoUsuario("Invitado", "invitado_pass");
        int idInvitado = ConexionBD.getConexionBD().verificarUsuarioYObtenerId("Invitado", "invitado_pass");

        if(idInvitado != -1){
            GestorTareas.getGestorTareas().setIdUsuarioLogueado(idInvitado);

            // Sincronizamos también el invitado con la carga de preferencias y tareas
            ConexionBD.getConexionBD().cargarDatosUsuario(idInvitado);
            ConexionBD.getConexionBD().cargarDatosDeBD(idInvitado);

            Platform.runLater(() -> {
                try {
                    Stage stagePrincipal = view.getPrimaryStage();
                    stagePrincipal.setOnCloseRequest(event -> {
                        event.consume();
                        stagePrincipal.hide();
                    });
                    view.showInitialView();
                    GestorTareas.getGestorTareas().verificarTareasHoy();
                } catch (Exception e) {
                    err.setText("Error al entrar como Invitado");
                }
            });
        }
    }

    @FXML
    private void abrirRegistroNuevoUsuario(){
        try {
            view.showCrearPIN();
        } catch (Exception e) {
            err.setText("Error al abrir la ventana de registro");
        }
    }
}