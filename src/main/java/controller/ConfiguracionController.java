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

    private static final String TEXTO_DARK = "dark-mode";  // Compliant

    @FXML
    private Pane rootPane;

    @FXML
    private CheckBox checkModoOscuro;

    @FXML
    private PasswordField txtNuevoPin;

    @FXML
    private ComboBox<String> boxFormatoHora;

    //Varaibles para actuar solo al darle a guardar y salir
    private boolean borrar=false;
    private boolean cambiarMdooVisual=false;
    private boolean descargarICS=false;

    private final Logger logger = Logger.getLogger(getClass().getName());

    //Llama al metodo para crear un CSV
    @FXML
    private void exportarACSV(){
        GestionEnFicheros.getGestionEnFicheros().exportarACSV();
    }

    //Rellena los huecos y los comboBox
    public void initialize(){
        boxIdioma.setItems(FXCollections.observableArrayList(Idiomas.values()));
        Idiomas idiomaActual=GestorTareas.getGestorTareas().getIdioma();
        boxIdioma.getSelectionModel().select(idiomaActual != null ? idiomaActual : Idiomas.ESPANIOL);

        boxFormatoHora.setItems(FXCollections.observableArrayList("24h", "12h"));

        String formatoGuardado = prefs.get("formato_hora", "24h");
        boxFormatoHora.getSelectionModel().select(formatoGuardado != null ? formatoGuardado : "24h");

        boolean isDark = prefs.getBoolean("modo_oscuro", false);
        checkModoOscuro.setSelected(isDark);
        // Si estaba guardado como oscuro, pintamos el rootPane
        if (isDark) rootPane.getStyleClass().add(TEXTO_DARK);
    }

    //Al clicar en el boton se pone a true, y solo se borra si se da a guardar y salir
    @FXML
    private void borrarTodo(){
        borrar=true;
    }

    //Llama al gestorTareas para eliminar  el contenido, solo se llama si borrar esta a true
    private void borrarSeguroSI(){
        GestorTareas.getGestorTareas().borrarContenido();
    }

    //Cierra la ventana como si nada hubiera pasado
    @FXML
    private void cancelar(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    //Guarda la configuracion
    @FXML
    private void guardarYCerrar(){
        //Para guardar la configuracion en el ordenador, idea de internet
        Idiomas idiomaSeleccionado = boxIdioma.getValue();
        prefs.put("idioma_actual", idiomaSeleccionado.getCodigo());

        prefs.put("formato_hora", boxFormatoHora.getValue());

        int idUsuario=GestorTareas.getGestorTareas().getIdUsuarioLogueado() ;

        //Si se ha pulsado algun boton se hace la accion
        if(borrar) borrarSeguroSI();
        if(cambiarMdooVisual) cambiarModoVisualSeguro();
        if(descargarICS) descargarICSSeguro();
        if (idUsuario != -1) ConexionBD.getConexionBD().guardarDatosUsuario(idUsuario,idiomaSeleccionado.getCodigo(),checkModoOscuro.isSelected());
        //Se establece el idioma y se cierra la ventana
        GestorTareas.getGestorTareas().setIdioma(idiomaSeleccionado);
        cancelar();

        try {
            View.showInitialView();
        } catch (Exception e) {
            logger.info("Error al recargar el menú principal: " + e.getMessage());
        }
    }

    //Recarga un archivo backup guardado, el ultimo
    @FXML
    private void restaurarBackup() throws IOException {
        GestionEnFicheros gf= GestionEnFicheros.getGestionEnFicheros();

        File ultimasEtiquetas = gf.obtenerUltimoBackup("etiquetas_backup_");

        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        //Aparece una vetana de alerta para confirmar
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("configuracion.alert.titulo"));
        alert.setContentText(bundle.getString("configuracion.alert.restaurar.mensaje"));
        if (alert.showAndWait().get() == ButtonType.OK) {
            GestorTareas.getGestorTareas().borrarContenido();
            gf.leerFicheroTareas();
            if (ultimasEtiquetas != null) gf.leerEtiquetas();
            View.showInitialView();
        }
    }

    //Al darle al boton, se pone a true indicando que se quiere cambiar
    @FXML
    private void cambiarModoVisual(){cambiarMdooVisual=true;}

    //Se ejecuta si la variable esta a true
    private void cambiarModoVisualSeguro(){
        boolean activado = checkModoOscuro.isSelected();
        // Se guarda la decisión en Preferences para el futuro
        prefs.putBoolean("modo_oscuro", activado);

        //Se recorren todas las ventanas aberitas y se les cambia el color
        for (Window ventanaAbierta : Window.getWindows()) {
            if (ventanaAbierta.getScene() != null && ventanaAbierta.getScene().getRoot() != null && activado) {
                    if (!ventanaAbierta.getScene().getRoot().getStyleClass().contains(TEXTO_DARK)) ventanaAbierta.getScene().getRoot().getStyleClass().add(TEXTO_DARK);
                    else ventanaAbierta.getScene().getRoot().getStyleClass().remove(TEXTO_DARK);
            }
        }
    }

    //Crea el archivo ics
    @FXML
    private void crearICS(){
        Stage ventanaPrincipal = (Stage) rootPane.getScene().getWindow();
        GestionEnFicheros.getGestionEnFicheros().exportarAICS(ventanaPrincipal);
    }

    //Al darle al boton se actualiza la variable y se escoge el fichero a leer, pero aun no se lee
    @FXML
    private void descargarICS(){
        descargarICS=true;
        Stage ventanaPrincipal = (Stage) rootPane.getScene().getWindow();
        GestionEnFicheros.getGestionEnFicheros().elegirArchivoICSLEER(ventanaPrincipal);
    }

    //Al darle a guardar y salir es cuando se lee, por si se confunde
    private void descargarICSSeguro(){
        GestionEnFicheros.getGestionEnFicheros().leerArchivoICS();
    }

    @FXML
    private void cerrarSesion(){
    try {
        GestorTareas.getGestorTareas().cerrarSesion();
        Stage ventanaActual = (Stage) rootPane.getScene().getWindow();
        ventanaActual.close();

        // 4. Ordenamos a la vista reabrir la ventana de Login/Bloqueo
        View.showPINInsert();
    } catch (Exception e) {
        logger.info("Error al intentar cambiar de usuario: " + e.getMessage());}
    }

    @FXML
    private void eliminarPerfil(){

        int idActivo=GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        if (idActivo == -1) return;

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Perfil");
        alert.setHeaderText("¿Estás seguro de que quieres eliminar tu cuenta?");
        alert.setContentText("Esta acción es irreversible y borrará para siempre todas tus tareas y etiquetas.");

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {

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
            mostrarAlerta("Campo vacío", "Por favor, introduce un nuevo PIN.", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }else if (nuevoPin.length() <= 4) {
            //longitud maxima de la contraseña
            mostrarAlerta("Seguridad", "El PIN de seguridad debe tener exactamente 4 dígitos.", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }
        boolean operacion=ConexionBD.getConexionBD().cambiarPin(nuevoPin);
        if(operacion){
            mostrarAlerta("Éxito", "¡PIN actualizado y base de datos re-cifrada correctamente!", javafx.scene.control.Alert.AlertType.INFORMATION);
            txtNuevoPin.clear();
        }
        else mostrarAlerta("Error", "No se pudo actualizar el PIN. Comprueba la conexión.", javafx.scene.control.Alert.AlertType.ERROR);
    }

    private void mostrarAlerta(String titulo, String mensaje, javafx.scene.control.Alert.AlertType tipo) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}