package Controller;

import Model.GestionEnFicheros;
import Model.GestorTareas;
import Model.Idiomas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    private final Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);

    @FXML
    private ComboBox<Idiomas> boxIdioma;

    @FXML
    private Button botonCancelar;

    @FXML
    private Pane rootPane;

    @FXML
    private CheckBox checkModoOscuro;

    @FXML
    private ComboBox<String> boxFormatoHora;

    //Varaibles para actuar solo al darle a guardar y salir
    private boolean borrar=false;
    private boolean cambiarMdooVisual=false;
    private boolean descargarICS=false;

    //Llama al metodo para crear un CSV
    @FXML
    private void exportarACSV(){
        GestionEnFicheros.getGestionEnFicheros().exportarACSV();
    }

    //Rellena los huecos y los comboBox
    public void initialize(){
        boxIdioma.setItems(FXCollections.observableArrayList(Idiomas.values()));
        Idiomas idiomaActual=GestorTareas.getGestorTareas().getIdioma();
        boxIdioma.getSelectionModel().select(idiomaActual != null ? idiomaActual : Idiomas.Espaniol);

        boxFormatoHora.setItems(FXCollections.observableArrayList("24h", "12h"));

        String formatoGuardado = prefs.get("formato_hora", "24h");
        boxFormatoHora.getSelectionModel().select(formatoGuardado != null ? formatoGuardado : "24h");

        boolean isDark = prefs.getBoolean("modo_oscuro", false);
        checkModoOscuro.setSelected(isDark);
        // Si estaba guardado como oscuro, pintamos el rootPane
        if (isDark) rootPane.getStyleClass().add("dark-mode");
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

        //Si se ha pulsado algun boton se hace la accion
        if(borrar) borrarSeguroSI();
        if(cambiarMdooVisual) cambiarModoVisualSeguro();
        if(descargarICS) descargarICSSeguro();

        //Se establece el idioma y se cierra la ventana
        GestorTareas.getGestorTareas().setIdioma(idiomaSeleccionado);
        cancelar();

        try {
            View.view.showInitialView();
        } catch (Exception e) {
            System.out.println("Error al recargar el menú principal: " + e.getMessage());
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
            View.view.showInitialView();
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
            if (ventanaAbierta.getScene() != null && ventanaAbierta.getScene().getRoot() != null) {
                // Aplicamos o quitamos la clase "dark-mode" a la raíz de cada ventana
                if (activado){
                    if (!ventanaAbierta.getScene().getRoot().getStyleClass().contains("dark-mode")) ventanaAbierta.getScene().getRoot().getStyleClass().add("dark-mode");
                 else ventanaAbierta.getScene().getRoot().getStyleClass().remove("dark-mode");
            }
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
}