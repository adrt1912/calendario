package Controller;

import Model.GestorTareas;
import View.view;
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
    /*
      DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

        textoReceptor.setText("Dime titulo");
        String titulo = textoReceptor.getText();
        textoReceptor.setText("Fecha fin, introducir en formato dd/mm/yyyy");
        String fechaText = textoReceptor.getText();
        LocalDate fechaFin;
        try {
            fechaFin = LocalDate.parse(fechaText);
        } catch (Exception e) {
            textoReceptor.setText("La fecha esta mal introducida, se considerara vacia");
            fechaFin = null;
        }

        textoReceptor.setText("Hora, introducir en formato HH:mm");
        LocalTime time;
        String horaText=textoReceptor.getText();
        try {
            time=LocalTime.parse(horaText);
        } catch (Exception e) {
            textoReceptor.setText("La hora esta mal introducida, se considerara vacia");
            time=null;
        }
        textoReceptor.setText("Sitio: ");
        String sitio=textoReceptor.getText();
        textoReceptor.setText("Descripcion: ");
        String descripcion=textoReceptor.getText();

        textoReceptor.setText("Quieres que sea periodica, si es asi introducir frecuencia");
        String perioricidad= (textoReceptor.getText());

            gestorTareas.anadirTarea(titulo,fechaFin,descripcion,sitio,time,perioricidad);

     */
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
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

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
