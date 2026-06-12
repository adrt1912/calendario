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
    private ComboBox boxIdioma;

    @FXML
    private ComboBox boxFormatoHora;

    @FXML
    private void exportarACSV(){
        GestionEnFicheros.getGestionEnFicheros().exportarACSV();
    }

    public void initialize(){
        boxIdioma.setItems(FXCollections.observableArrayList(Idiomas.values()));
        Idiomas idiomas=GestorTareas.getGestorTareas().getIdioma();
        if(idiomas!=null)  boxIdioma.getSelectionModel().select(idiomas); // Para dejar uno marcado por defecto
        else boxIdioma.getSelectionModel().select(Idiomas.ESPAÑOL);
    }

    @FXML
    private void borrarTodo(){
        GestorTareas.getGestorTareas().borrarContenido();
    }
    @FXML
    private Button botonCancelar;
    @FXML
    private void cancelar(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    @FXML
    private void guardarYCerrar(){

        //Para guardar la configuracion en el ordenador, idea de internet
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        Idiomas idiomaSeleccionado = (Idiomas) boxIdioma.getValue();
        if (idiomaSeleccionado == Idiomas.INGLES) {
            prefs.put("idioma_actual", "en");
        } else {
            prefs.put("idioma_actual", "es");
        }
        GestorTareas.getGestorTareas().setIdioma(idiomaSeleccionado);
        cancelar();

        try {
            View.view.showInitialView();
        } catch (Exception e) {
            System.out.println("Error al recargar el menú principal: " + e.getMessage());
        }
        GestorTareas.getGestorTareas().setIdioma((Idiomas) boxIdioma.getValue());
    }
}
