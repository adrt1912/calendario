package Controller;

import Model.*;
import View.view;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
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
private ComboBox campoPerioricidad;
@FXML
private TextArea campoDescripcion;
@FXML
private CheckBox checkCompletada;
@FXML
private Label textoTituloTop;
@FXML
private ComboBox boxEtiquetas;
@FXML
private Text textoError;

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
        if(tareaMos.getDescripcion()!=null){
            campoDescripcion.setText(tareaMos.getDescripcion());
        }
        if(tareaMos.getEstadoTarea().equals(EstadoTarea.COMPLETADA)){
            checkCompletada.setSelected(true);
        }
    campoPerioricidad.getItems().addAll(Periodicidad.values());
    boxEtiquetas.getItems().addAll(GestorTareas.getGestorTareas().getListaEtiquetas());
    textoTituloTop.setText(tareaMos.getNombreTarea());
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
private void eliminarTarea(){
    if(tareaMos.getFrecuencia()!=Periodicidad.NUNCA){
        try{
            view.showConfirmacionEl(tareaMos);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }
    GestorTareas.getGestorTareas().eliminarTarea(tareaMos);
    cerrarTodo();
}

@FXML
private void modificarTarea() {
    DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    String titulo = campoTitulo.getText();
    LocalDate fechaFin = campoFecha.getValue();
    if (fechaFin == null) {
        fechaFin = LocalDate.now();
    }
    String descripcion = campoDescripcion.getText();
    String sitio = campoSitio.getText();
    LocalTime time;
    String horaText = campoHora.getText();
    try {
        time = LocalTime.parse(horaText);
    } catch (Exception e) {
        time = null;
    }

    Periodicidad frecuencia = (Periodicidad) campoPerioricidad.getValue();
    if (frecuencia == null) {
        frecuencia = Periodicidad.NUNCA; // Le damos un valor por defecto
    }
    EstadoTarea estado = tareaMos.getEstadoTarea();

    Etiqueta etiqueta = (Etiqueta) boxEtiquetas.getValue();

    if (titulo.isBlank()) {
        textoError.setText("Introduzca un titulo");
    } else {
        GestorTareas.getGestorTareas().modificarTarea(tareaMos, titulo, fechaFin, descripcion, sitio, time, frecuencia, estado, etiqueta);
        cerrarTodo();
    }
}

}