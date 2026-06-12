package Controller;

import Model.Etiqueta;
import Model.GestorTareas;
import Model.Periodicidad;
import Model.Tarea;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class CrearTareaController {
    @FXML
    private Button botonCancelar;

    @FXML
    private Text textoError;

    @FXML
    private Button botonGuardar;

    public void initialize(LocalDate fechaf){
        textoPeriodicidad.getItems().addAll(Periodicidad.values());
        boxEtiquetas.getItems().addAll(GestorTareas.getGestorTareas().getListaEtiquetas());
        if(fechaf!=null) texFecha.setValue(fechaf);
    }
    @FXML
    private TextField textoTitulo;

    @FXML
    private DatePicker texFecha;

    @FXML
    private TextField textoHora;

    @FXML
    private TextField textoSitio;

    @FXML
    private ComboBox textoPeriodicidad;

    @FXML
    private TextArea textoDescripcion;

    @FXML
    private ComboBox boxEtiquetas;

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
        Periodicidad frecuencia= (Periodicidad) textoPeriodicidad.getValue();
        if (frecuencia == null) {
            frecuencia = Periodicidad.NUNCA; // Le damos un valor por defecto
        }
        String descripcion=textoDescripcion.getText();

        Etiqueta etiqueta= (Etiqueta) boxEtiquetas.getValue();

        if(titulo.isBlank()){
            textoError.setText("Introduzca un titulo");
        }else{
        Tarea tarea=GestorTareas.getGestorTareas().anadirTarea(titulo,fecha,descripcion,sitio,time,frecuencia,titulo,etiqueta);
        if(tarea!=null) tratarTareasPeriodicas(tarea);
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();}
    }


    private void tratarTareasPeriodicas(Tarea tarea){
            int dias=tarea.getFrecuencia().getDias();
            int mes=tarea.getFrecuencia().getMes();
            int anio=tarea.getFrecuencia().getAnios();
            for(int i=0;i<40;i++){
                LocalDate fecha=tarea.getFechaFin().plusDays((long) i *dias).plusMonths((long) i *mes).plusYears((long) i *anio);
                GestorTareas.getGestorTareas().anadirTarea(tarea.getNombreTarea(),fecha,tarea.getDescripcion(),tarea.getSitio(),tarea.getHora(),tarea.getFrecuencia(), tarea.getIdTarea(),tarea.getEtiqueta());
            }

    }

    @FXML
    private void cancelarTodo(){

        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }
}