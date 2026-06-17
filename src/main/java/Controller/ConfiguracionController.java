package Controller;

import Model.GestionEnFicheros;
import Model.GestorTareas;
import Model.Idiomas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ConfiguracionController {

    @FXML
    private ComboBox<Idiomas> boxIdioma;

    @FXML
    private ComboBox<String> boxFormatoHora;

    //Llama al metodo para crear un CSV
    @FXML
    private void exportarACSV(){
        GestionEnFicheros.getGestionEnFicheros().exportarACSV();
    }

    //Rellena los huecos y los comboBox
    public void initialize(){
        boxIdioma.setItems(FXCollections.observableArrayList(Idiomas.values()));
        Idiomas idiomas=GestorTareas.getGestorTareas().getIdioma();
        if(idiomas!=null)  boxIdioma.getSelectionModel().select(idiomas); // Para dejar uno marcado por defecto
        else boxIdioma.getSelectionModel().select(Idiomas.ESPAÑOL);

        boxFormatoHora.setItems(FXCollections.observableArrayList("24h", "12h"));

        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String formatoGuardado = prefs.get("formato_hora", "24h");
        if (formatoGuardado != null) boxFormatoHora.getSelectionModel().select(formatoGuardado); // Selecciona el que toca (12h o 24h)
        else boxFormatoHora.getSelectionModel().select("24h");
         prefs = Preferences.userNodeForPackage(this.getClass());
        boolean isDark = prefs.getBoolean("modo_oscuro", false);

        checkModoOscuro.setSelected(isDark);

        // Si estaba guardado como oscuro, pintamos el rootPane
        if (isDark) {
            // Como a veces el rootPane tarda unos milisegundos en cargar su CSS, es buena práctica hacer esto:
            rootPane.getStyleClass().add("dark-mode");
        }

    }

    //Llama al gestorTareas para eliminar  el contenido
    @FXML
    private void borrarTodo(){
        GestorTareas.getGestorTareas().borrarContenido();
    }

    @FXML
    private Button botonCancelar;
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
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        Idiomas idiomaSeleccionado = (Idiomas) boxIdioma.getValue();
        if (idiomaSeleccionado == Idiomas.INGLES) prefs.put("idioma_actual", "en");
        else if(idiomaSeleccionado==Idiomas.FRANCES) prefs.put("idioma_actual","fr");
        else if(idiomaSeleccionado==Idiomas.EUSKERA)prefs.put("idioma_actual","eu");
        else if(idiomaSeleccionado==Idiomas.ALEMAN) prefs.put("idioma_actual","de");
        else prefs.put("idioma_actual", "es");

        if(boxFormatoHora.getValue().equals("24h")) prefs.put("formato_hora",boxFormatoHora.getValue().toString());
        else prefs.put("formato_hora",boxFormatoHora.getValue().toString());

        GestorTareas.getGestorTareas().setIdioma(idiomaSeleccionado);
        cancelar();

        try {
            View.view.showInitialView();
        } catch (Exception e) {
            System.out.println("Error al recargar el menú principal: " + e.getMessage());
        }
    }

    @FXML
    private void restaurarBackup() throws IOException {
        GestionEnFicheros gf= GestionEnFicheros.getGestionEnFicheros();

        File ultimoTareas = gf.obtenerUltimoBackup("tareas_backup_");
        File ultimasEtiquetas = gf.obtenerUltimoBackup("etiquetas_backup_");

        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("configuracion.alert.titulo"));
        alert.setContentText(bundle.getString("configuracion.alert.restaurar.mensaje"));
        if (alert.showAndWait().get() == ButtonType.OK) {
            // 3. Vaciar y cargar
            GestorTareas.getGestorTareas().borrarContenido();
            gf.leerFicheroTareas(ultimoTareas);

            if (ultimasEtiquetas != null) {
                gf.leerEtiquetas(ultimasEtiquetas);
            }
            View.view.showInitialView();
        }
    }
    @FXML
    private Pane rootPane;

    @FXML
    private CheckBox checkModoOscuro;
    @FXML
    private void cambiarModoVisual(){


            boolean activado = checkModoOscuro.isSelected();

            // 1. Guardamos la decisión en Preferences para el futuro
            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            prefs.putBoolean("modo_oscuro", activado);

            // 2. ACTUALIZACIÓN GLOBAL: Recorremos TODAS las ventanas abiertas en tiempo real
            for (javafx.stage.Window ventanaAbierta : javafx.stage.Window.getWindows()) {
                if (ventanaAbierta.getScene() != null && ventanaAbierta.getScene().getRoot() != null) {

                    // Aplicamos o quitamos la clase "dark-mode" a la raíz de cada ventana
                    if (activado) {
                        if (!ventanaAbierta.getScene().getRoot().getStyleClass().contains("dark-mode")) {
                            ventanaAbierta.getScene().getRoot().getStyleClass().add("dark-mode");
                        }
                    } else {
                        ventanaAbierta.getScene().getRoot().getStyleClass().remove("dark-mode");
                    }
                }

        }
    }
}