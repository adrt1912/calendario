package controller;

import model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CrearTareaController {
    @FXML
    private Button botonCancelar;

    @FXML
    private Text textoError;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField textoTitulo;

    @FXML
    private DatePicker texFecha;

    @FXML
    private TextField textoHoraInicio;

    @FXML
    private TextField textoHoraFin;

    @FXML
    private TextField textoSitio;

    @FXML
    private ComboBox<Periodicidad> textoPeriodicidad;

    @FXML
    private TextArea textoDescripcion;

    @FXML
    private DatePicker textFechaFin;

    @FXML
    private ComboBox<Etiqueta> boxEtiquetas;

    //Initialize por defecto
    public void initialize(){
        //Pone el idioma y el modo oscuro o claro
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) rootPane.getStyleClass().add("dark-mode");

        //Rellena los textos con los box
        textoPeriodicidad.setItems(FXCollections.observableArrayList(Periodicidad.values()));
        textoPeriodicidad.getSelectionModel().select(Periodicidad.NUNCA);

        boxEtiquetas.setItems(FXCollections.observableArrayList(GestorTareas.getGestorTareas().getListaEtiquetas()));
        boxEtiquetas.getSelectionModel().select(GestorTareas.getGestorTareas().getEtiquetaNeutra());

        // Listener inteligente para la coherencia de fechas, para que la fin no sea antes que la inicio
        texFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null&&(textFechaFin.getValue() == null || textFechaFin.getValue().isBefore(newValue))) textFechaFin.setValue(newValue);
        });
    }

    //initialize si se carga desde un dia elegido, recibe la fecha y hora
    public void initialize(LocalDate fechaf,LocalTime hora){
        //Si la fecha no es nula se pone en los textos
        if(fechaf!=null){
            texFecha.setValue(fechaf);
            textFechaFin.setValue(fechaf);
        }
        //Si la hora no es nula se pone en el texto
        if(hora!=null) textoHoraInicio.setText(hora.toString());
    }

    //Al darle a crear crea y guarda la tarea, leyendo todos los datos
    @FXML
    private void guardarTarea() {
        LocalDate fechaInicio = texFecha.getValue();
        LocalDate fechaFin=textFechaFin.getValue();
        String titulo = textoTitulo.getText();

        LocalTime horaInicio;
        String horaI = textoHoraInicio.getText(); // Capturamos el texto de inicio

        //Se comprueba que esta bien escrito, sino es nula
        try {
            horaInicio = LocalTime.parse(horaI);
        } catch (Exception e) {
            try {
                DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                horaInicio = LocalTime.parse(horaI, formato12h);
            } catch (Exception ex) {
                horaInicio = null;
            }
        }
        //Similar a la hora inicio
        LocalTime horaFin;
        String horaF = textoHoraFin.getText(); // Capturamos el texto de FIN (Aquí estaba el fallo)

        try {
            horaFin = LocalTime.parse(horaF);
        } catch (Exception e) {
            try {
                DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                horaFin = LocalTime.parse(horaF, formato12h);
            } catch (Exception ex) {
                horaFin = null;
            }
        }

        String sitio = textoSitio.getText();
        Periodicidad frecuencia = textoPeriodicidad.getValue();
        //Si no se da valor a la frecuencia se pone una por defecto
        if (frecuencia == null) {
            frecuencia = Periodicidad.NUNCA;
        }
        String descripcion = textoDescripcion.getText();
        Etiqueta etiqueta = boxEtiquetas.getValue();

        //Se oblica a que tenga un titulo
        if (titulo.isBlank()) {
            textoError.setText("Introduzca un titulo");
        } else {
            //se crea la tarea
            String idFamiliaUnico = java.util.UUID.randomUUID().toString();
            Tarea tarea = GestorTareas.getGestorTareas().anadirTarea(new TareaDatos(titulo, fechaInicio,fechaFin, descripcion, sitio, horaInicio, horaFin, frecuencia, idFamiliaUnico, etiqueta));

            if (tarea != null && frecuencia != Periodicidad.NUNCA) tratarTareasPeriodicas(tarea);
            Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
            ventanaActual.close();
        }
    }

    //Con las tareas periodicas (distintas a NUNCA) se hacen 40 tareas sumando el tiempo de la periodicidad
    private void tratarTareasPeriodicas(Tarea tarea){
        int dias=tarea.getFrecuencia().getDias();
        int mes=tarea.getFrecuencia().getMes();
        int anio=tarea.getFrecuencia().getAnios();
        for(int i=1;i<40;i++){
            LocalDate fechaNuevoInicio=tarea.getFechaInicio().plusDays((long) i *dias).plusMonths((long) i *mes).plusYears((long) i *anio);
            LocalDate nuevafechaFin=tarea.getFechaFin().plusDays((long)i*dias).plusMonths((long)i*mes).plusYears((long) i*anio);
            GestorTareas.getGestorTareas().anadirTarea(new TareaDatos(tarea.getNombreTarea(), fechaNuevoInicio, nuevafechaFin, tarea.getDescripcion(), tarea.getSitio(), tarea.getHoraInicio(), tarea.getHoraFin(), tarea.getFrecuencia(), tarea.getIdFamilia(), tarea.getEtiqueta()));
        }
    }

    //Solo cierra la ventana
    @FXML
    private void cancelarTodo(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }
}