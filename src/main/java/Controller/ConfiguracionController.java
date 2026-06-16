package Controller;

import Model.GestionEnFicheros;
import Model.GestorTareas;
import Model.Idiomas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

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
}