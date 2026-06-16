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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CrearTareaController {
    @FXML
    private Button botonCancelar;

    @FXML
    private Text textoError;

    public void initialize(LocalDate fechaf){
        //Se le pasa la fecha e inicia los objetos
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
    private ComboBox<Periodicidad> textoPeriodicidad;

    @FXML
    private TextArea textoDescripcion;

    @FXML
    private ComboBox<Etiqueta> boxEtiquetas;

    //Al ser creada lee los datos y los guarda
    @FXML
    private void guardarTarea(){
        LocalDate fecha=texFecha.getValue();
        String titulo=textoTitulo.getText();
        LocalTime time;
        String hora=textoHora.getText();

        try {
            time = LocalTime.parse(hora);
        } catch (Exception e) {
            try {
                DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                time = LocalTime.parse(hora, formato12h);
            } catch (Exception ex) {
                time = null;
            }
        }
        String sitio=textoSitio.getText();
        Periodicidad frecuencia= textoPeriodicidad.getValue();
        if (frecuencia == null) {
            frecuencia = Periodicidad.NUNCA; // Le damos un valor por defecto
        }
        String descripcion=textoDescripcion.getText();
        Etiqueta etiqueta= boxEtiquetas.getValue();

        if(titulo.isBlank()){
            textoError.setText("Introduzca un titulo");
        }else{
            //Si es el unico de su familia, se crea uno nuevo
            String idFamiliaUnico = java.util.UUID.randomUUID().toString();
        Tarea tarea=GestorTareas.getGestorTareas().anadirTarea(titulo,fecha,descripcion,sitio,time,frecuencia,idFamiliaUnico,etiqueta);
        if(tarea!=null && frecuencia!=Periodicidad.NUNCA) tratarTareasPeriodicas(tarea);
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();}
    }

//Para tareas periodicas se crean 40, y se crea una nueva, sumando el plazo
    private void tratarTareasPeriodicas(Tarea tarea){
            int dias=tarea.getFrecuencia().getDias();
            int mes=tarea.getFrecuencia().getMes();
            int anio=tarea.getFrecuencia().getAnios();
            for(int i=1;i<40;i++){
                LocalDate fecha=tarea.getFechaFin().plusDays((long) i *dias).plusMonths((long) i *mes).plusYears((long) i *anio);
                GestorTareas.getGestorTareas().anadirTarea(tarea.getNombreTarea(),fecha,tarea.getDescripcion(),tarea.getSitio(),tarea.getHora(),tarea.getFrecuencia(), tarea.getIdTarea(),tarea.getEtiqueta());
            }
    }

    //Para no guardar nada, solo cierra la pestaña
    @FXML
    private void cancelarTodo(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }
}