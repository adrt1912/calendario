package Controller;

import Model.GestorTareas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CrearTareaController {
    @FXML
    private Button botonCancelar;

    @FXML
    private Button botonGuardar;

    public void initialize(){

    }
    @FXML
    private TextField textoTitulo;

    @FXML
    private DatePicker texFecha;

    @FXML
    private TextField textoHora;

    @FXML
    private  TextField textoSitio;

    @FXML
    private TextField textoPeriodicidad;

    @FXML
    private TextArea textoDescripcion;

    @FXML
    private void guardarTarea(){
        LocalDate fecha=texFecha.getValue();

        String titulo=textoTitulo.getText();

        LocalTime time;
        String hora=textoHora.getText();

        try {
            time=LocalTime.parse(hora);
        } catch (Exception e) {
            time=null;
        }
        String sitio=textoSitio.getText();
        String frecuencia=textoPeriodicidad.getText();
        String descripcion=textoDescripcion.getText();

        GestorTareas.getGestorTareas().anadirTarea(titulo,fecha,descripcion,sitio,time,frecuencia);
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    @FXML
    private void cancelarTodo(){

        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }
}