package Controller;

import Model.GestorTareas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class crearEtiquetaController {

    @FXML
    private TextField cuadroNombre;

    @FXML
    private ColorPicker colorEtiqueta;

    @FXML
    private Button botonCancelar;

    //Solo cierra la ventana
    @FXML
    private void cancelarEtiqueta(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    @FXML
    private AnchorPane rootPane;

    public void initialize(){
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ConfiguracionController.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    @FXML
    private Text textoError;
    //Crea la etiqueta
    @FXML
    private void crearEtiqueta(){
        String nombreEtiqueta=cuadroNombre.getText();
        String colorEtiqueta="#"+this.colorEtiqueta.getValue().toString().substring(2,8);
        //El nombre de la etiqueta no puede ser null ni estar en blanco
        if (nombreEtiqueta == null || nombreEtiqueta.isBlank()) textoError.setText("Error al crear etiqueta, hay campos sin rellenar");
            //El nombre de la etiqueta no puede ser "Sin Etiqueta" es el que se usa como neutra
        else if (nombreEtiqueta.trim().equalsIgnoreCase("Sin Etiqueta")) textoError.setText("Error: Ese nombre está reservado por el sistema.");
        else if( GestorTareas.getGestorTareas().getListaEtiquetas().stream() .anyMatch(e -> e.getNombreEtiqueta() != null && e.getNombreEtiqueta().equalsIgnoreCase(nombreEtiqueta))) textoError.setText("Error: Ya existe una etiqueta con este nombre.");
        else{
            GestorTareas.getGestorTareas().nuevaEtiqueta( nombreEtiqueta,colorEtiqueta);
            cancelarEtiqueta();}
    }
}