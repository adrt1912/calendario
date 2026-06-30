package controller;

import model.ConexionBD;
import model.GestionEnFicheros;
import model.GestorTareas;
import model.Idiomas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import view.View;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    private final Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);

    @FXML
    private ComboBox<Idiomas> boxIdioma;

    @FXML
    private Button botonCancelar;

    private static final String TEXTO_DARK = "dark-mode";

    @FXML
    private Pane rootPane;

    @FXML
    private CheckBox checkModoOscuro;

    @FXML
    private PasswordField txtNuevoPin;

    @FXML
    private ComboBox<String> boxFormatoHora;

    private boolean borrar=false;
    private boolean cambiarMdooVisual=false;
    private boolean descargarICS=false;

    private final Logger logger = Logger.getLogger(getClass().getName());

    @FXML
    private void exportarACSV(){
        GestionEnFicheros.getGestionEnFicheros().exportarACSV();
    }

    public void initialize(){
        boxIdioma.setItems(FXCollections.observableArrayList(Idiomas.values()));
        Idiomas idiomaActual=GestorTareas.getGestorTareas().getIdioma();
        boxIdioma.getSelectionModel().select(idiomaActual != null ? idiomaActual : Idiomas.ESPANIOL);

        boxFormatoHora.setItems(FXCollections.observableArrayList("24h", "12h"));

        String formatoGuardado = prefs.get("formato_hora", "24h");
        boxFormatoHora.getSelectionModel().select(formatoGuardado != null ? formatoGuardado : "24h");

        boolean isDark = prefs.getBoolean("modo_oscuro", false);
        checkModoOscuro.setSelected(isDark);
        if (isDark) rootPane.getStyleClass().add(TEXTO_DARK);
    }

    @FXML
    private void borrarTodo(){
        borrar=true;
    }

    private void borrarSeguroSI(){
        GestorTareas.getGestorTareas().borrarContenido();
    }

    @FXML
    private void cancelar(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    @FXML
    private void guardarYCerrar(){
        Idiomas idiomaSeleccionado = boxIdioma.getValue();
        prefs.put("idioma_actual", idiomaSeleccionado.getCodigo());

        prefs.put("formato_hora", boxFormatoHora.getValue());

        int idUsuario=GestorTareas.getGestorTareas().getIdUsuarioLogueado() ;

        if(borrar) borrarSeguroSI();
        if(cambiarMdooVisual) cambiarModoVisualSeguro();
        if(descargarICS) descargarICSSeguro();
        if (idUsuario != -1) ConexionBD.getConexionBD().guardarDatosUsuario(idUsuario,idiomaSeleccionado.getCodigo(),checkModoOscuro.isSelected());

        GestorTareas.getGestorTareas().setIdioma(idiomaSeleccionado);
        cancelar();

        try {
            View.showInitialView();
        } catch (Exception e) {
            logger.info("Error al recargar el menú principal: " + e.getMessage());
        }
    }

    @FXML
    private void restaurarBackup() throws IOException {
        GestionEnFicheros gf = GestionEnFicheros.getGestionEnFicheros();

        // 1. Buscamos y guardamos la referencia de los últimos archivos físicos reales existentes antes de limpiar la app
        File ultimoBackupTareas = gf.obtenerUltimoBackup("tareas_backup_");
        File ultimasEtiquetas = gf.obtenerUltimoBackup("etiquetas_backup_");

        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("configuracion.alert.titulo"));
        alert.setContentText(bundle.getString("configuracion.alert.restaurar.mensaje"));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Limpiamos las listas de la memoria RAM activa
            GestorTareas.getGestorTareas().borrarContenido();

            // 2. Inyectamos los archivos recuperados de forma explícita hacia los lectores
            if (ultimoBackupTareas != null) gf.leerFicheroTareas(ultimoBackupTareas);

            if (ultimasEtiquetas != null) gf.leerEtiquetas(ultimasEtiquetas);

            View.showInitialView();
        }
    }

    @FXML
    private void cambiarModoVisual(){cambiarMdooVisual=true;}

    private void cambiarModoVisualSeguro(){
        boolean activado = checkModoOscuro.isSelected();
        prefs.putBoolean("modo_oscuro", activado);

        for (Window ventanaAbierta : Window.getWindows()) {
            if (ventanaAbierta.getScene() != null && ventanaAbierta.getScene().getRoot() != null) {
                if (activado) {
                    if (!ventanaAbierta.getScene().getRoot().getStyleClass().contains(TEXTO_DARK)) {
                        ventanaAbierta.getScene().getRoot().getStyleClass().add(TEXTO_DARK);
                    }
                } else ventanaAbierta.getScene().getRoot().getStyleClass().remove(TEXTO_DARK);
            }
        }
    }

    @FXML
    private void crearICS(){
        Stage ventanaPrincipal = (Stage) rootPane.getScene().getWindow();
        GestionEnFicheros.getGestionEnFicheros().exportarAICS(ventanaPrincipal);
    }

    @FXML
    private void descargarICS(){
        descargarICS=true;
        Stage ventanaPrincipal = (Stage) rootPane.getScene().getWindow();
        GestionEnFicheros.getGestionEnFicheros().elegirArchivoICSLEER(ventanaPrincipal);
    }

    private void descargarICSSeguro(){
        GestionEnFicheros.getGestionEnFicheros().leerArchivoICS();
    }

    @FXML
    private void cerrarSesion(){
        try {
            GestorTareas.getGestorTareas().cerrarSesion();
            Stage ventanaActual = (Stage) rootPane.getScene().getWindow();
            ventanaActual.close();
            View.showPINInsert();
        } catch (Exception e) {
            logger.info("Error al intentar cambiar de usuario: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarPerfil(){
        int idActivo=GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        if (idActivo == -1) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Perfil");
        alert.setHeaderText("¿Estás seguro de que quieres eliminar tu cuenta?");
        alert.setContentText("Esta acción es irreversible y borrará para siempre todas tus tareas y etiquetas.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ConexionBD.getConexionBD().borrarPerfil(idActivo);
            GestorTareas.getGestorTareas().cerrarSesion();
            Stage ventanaActual = (Stage) rootPane.getScene().getWindow();
            ventanaActual.close();
            try {
                View.showPINInsert();
            } catch (Exception e) {
                logger.info("Error en la operación previa al cambio de PIN: "+e);
            }
        }
    }

    @FXML
    private void onCambiarPinClick() {
        String nuevoPin = txtNuevoPin.getText();

        if (nuevoPin == null || nuevoPin.isBlank()) {
            mostrarAlerta("Campo vacío", "Por favor, introduce un nuevo PIN.", Alert.AlertType.WARNING);
            return;
        } else if (nuevoPin.length() != 4) { // Corregido para validar exactamente 4 dígitos según tu comentario
            mostrarAlerta("Seguridad", "El PIN de seguridad debe tener exactamente 4 dígitos.", Alert.AlertType.WARNING);
            return;
        }

        boolean operacion = ConexionBD.getConexionBD().cambiarPin(nuevoPin);
        if(operacion){
            mostrarAlerta("Éxito", "¡PIN actualizado y base de datos re-cifrada correctamente!", Alert.AlertType.INFORMATION);
            txtNuevoPin.clear();
        } else mostrarAlerta("Error", "No se pudo actualizar el PIN. Comprueba la conexión.", Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}