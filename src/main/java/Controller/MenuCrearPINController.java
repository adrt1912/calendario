package Controller;

import Model.ConexionBD;
import Model.GestorTareas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MenuCrearPINController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private PasswordField campoContrasenia;

    @FXML
    private PasswordField campoConfirmarContrasenia;

    @FXML
    private Button botonCancelar;

    @FXML
    private Text textoError;

    @FXML
    private TextField campoNuevoUsuario;

    @FXML
    public void initialize() {
        // Heredamos el modo oscuro global para mantener la estética de la app
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    @FXML
    private void cancelar(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }


    @FXML
    private void guardarPIN(){
        String pin = campoContrasenia.getText();
        String confirmacion = campoConfirmarContrasenia.getText();
        String nomUsuario=campoNuevoUsuario.getText();

        // Cargamos el diccionario de idiomas activo (ES, EN, EU, FR, DE)
        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        if(pin==null||pin.isBlank()||confirmacion==null||confirmacion.isBlank())  textoError.setText(bundle.getString("establecerPin.error.vacio"));
        else if (pin.length() <= 3) textoError.setText(bundle.getString("establecerPin.error.corto"));
        else if (!pin.matches("\\d+")) textoError.setText(bundle.getString("establecerPin.error.numerico"));
        else if (!pin.equals(confirmacion)) textoError.setText(bundle.getString("establecerPin.error.noCoincide"));
        else {
            boolean registradoConExito = ConexionBD.getConexionBD().registrarNuevoUsuario(nomUsuario.trim(), pin);
            if (registradoConExito) cancelar();
                else textoError.setText("Ese nombre de usuario ya está registrado. Elige otro.");
        }
    }

}
