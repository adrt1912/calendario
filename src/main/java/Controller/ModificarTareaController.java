package Controller;

import Model.EstadoTarea;
import Model.GestorTareas;
import Model.Tarea;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;

public class ModificarTareaController {
    private Tarea tareaMos;
@FXML
private TextField campoTitulo;
@FXML
private DatePicker campoFecha;
@FXML
private TextField campoHora;
@FXML
private TextField campoSitio;
@FXML
private TextField campoPeriodicidad;
@FXML
private TextArea campoDescripcion;
@FXML
private CheckBox checkCompletada;

public void setTareaMos(Tarea tareaMos) {
        this.tareaMos = tareaMos;
        campoTitulo.setText(tareaMos.getNombreTarea());
        if(tareaMos.getFechaFin()!=null){
            campoFecha.setValue(tareaMos.getFechaFin());
        }
        if(tareaMos.getHora()!=null){
            campoHora.setText(tareaMos.getHora().toString());
        }
        if(tareaMos.getSitio()!=null){
            campoHora.setText(tareaMos.getSitio());
        }
        if(tareaMos.getFrecuencia()!=null){
            campoPeriodicidad.setText(tareaMos.getFrecuencia().toString());
        }
        if(tareaMos.getDescripcion()!=null){
            campoDescripcion.setText(tareaMos.getDescripcion());
        }
        if(tareaMos.getEstadoTarea().equals(EstadoTarea.COMPLETADA)){
            checkCompletada.setSelected(true);
        }
}
@FXML
private void cambiarEstado(){
    if(checkCompletada.isSelected()){
        tareaMos.setEstadoTarea(EstadoTarea.COMPLETADA);
    }else {
        tareaMos.setEstadoTarea(EstadoTarea.EN_PROCESO);}
}

@FXML
private Button botonCancelar;

@FXML
private void cerrarTodo(){
    Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
    ventanaActual.close();
}

@FXML
private Button botonEliminar;

@FXML
private void eliminarTarea(){
    GestorTareas.getGestorTareas().eliminarTarea(tareaMos);
    cerrarTodo();
}

@FXML
private void modificarTarea(){
    DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    String titulo=campoTitulo.getText();
    LocalDate fechaFin=campoFecha.getValue();
    if(fechaFin==null){fechaFin=LocalDate.now();}
    String descripcion=campoDescripcion.getText();
    String sitio= campoSitio.getText();
    LocalTime time;
    String horaText=campoHora.getText();
    try {
        time=LocalTime.parse(horaText);
    } catch (Exception e) {
        time=null;
    }
    String frecuencia=campoPeriodicidad.getText();
    if(frecuencia==null||frecuencia.isBlank()){
        frecuencia="0";
    }
    EstadoTarea estado=tareaMos.getEstadoTarea();

    GestorTareas.getGestorTareas().modificarTarea(tareaMos,titulo,fechaFin,descripcion,sitio,time,frecuencia,estado);
    cerrarTodo();
}
}