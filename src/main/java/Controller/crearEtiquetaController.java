package Controller;

import Model.GestorTareas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class crearEtiquetaController {

    @FXML
    private TextField cuadroNombre;

    @FXML
    private ColorPicker colorEtiqueta;

    @FXML
    private Button botonCancelar;

    @FXML
    private void cancelarEtiqueta(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    @FXML
    private Text textoError;
    @FXML
    private void crearEtiqueta(){
        String nombreEtiqueta=cuadroNombre.getText();
        String colorEtiqueta="#"+this.colorEtiqueta.getValue().toString().substring(2,8);

        if (nombreEtiqueta != null && !nombreEtiqueta.isBlank()) {
            GestorTareas.getGestorTareas().nuevaEtiqueta(colorEtiqueta, nombreEtiqueta);
            cancelarEtiqueta();
        }else {
            textoError.setText("Error al crear etiqueta, hay campos sin rellenar");
        }
    }
}
