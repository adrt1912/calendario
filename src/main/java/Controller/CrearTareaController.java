package Controller;

import Model.Etiqueta;
import Model.GestorTareas;
import Model.Periodicidad;
import Model.Tarea;
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

    public void initialize(){
        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionController.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
        texFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (textFechaFin.getValue() == null || textFechaFin.getValue().isBefore(newValue)) {
                    textFechaFin.setValue(newValue);
                }
            }
        });
    }

    public void initialize(LocalDate fechaf,LocalTime hora){
        textoPeriodicidad.getItems().addAll(Periodicidad.values());
        boxEtiquetas.getItems().addAll(GestorTareas.getGestorTareas().getListaEtiquetas());
        if(fechaf!=null){
            texFecha.setValue(fechaf);
            textFechaFin.setValue(fechaf);
        }
        if(hora!=null) textoHoraInicio.setText(hora.toString());

    }

    @FXML
    private void guardarTarea() {
        LocalDate fechaInicio = texFecha.getValue();
        LocalDate fechaFin=textFechaFin.getValue();
        String titulo = textoTitulo.getText();

        // --- 1. PROCESAR HORA INICIO ---
        LocalTime horaInicio;
        String horaI = textoHoraInicio.getText(); // Capturamos el texto de inicio

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

        // --- 2. PROCESAR HORA FIN ---
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
        if (frecuencia == null) {
            frecuencia = Periodicidad.NUNCA;
        }
        String descripcion = textoDescripcion.getText();
        Etiqueta etiqueta = boxEtiquetas.getValue();

        if (titulo.isBlank()) {
            textoError.setText("Introduzca un titulo");
        } else {
            String idFamiliaUnico = java.util.UUID.randomUUID().toString();
            // Asegúrate de que este método anadirTarea de tu GestorTareas acepta horaInicio y horaFin en este orden
            Tarea tarea = GestorTareas.getGestorTareas().anadirTarea(titulo, fechaInicio,fechaFin, descripcion, sitio, horaInicio, horaFin, frecuencia, idFamiliaUnico, etiqueta);
            if (tarea != null && frecuencia != Periodicidad.NUNCA) tratarTareasPeriodicas(tarea);
            Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
            ventanaActual.close();
        }
    }

    private void tratarTareasPeriodicas(Tarea tarea){
        int dias=tarea.getFrecuencia().getDias();
        int mes=tarea.getFrecuencia().getMes();
        int anio=tarea.getFrecuencia().getAnios();
        for(int i=1;i<40;i++){
            LocalDate fechaNuevoInicio=tarea.getFechaInicio().plusDays((long) i *dias).plusMonths((long) i *mes).plusYears((long) i *anio);
            LocalDate nuevafechaFin=tarea.getFechaFin().plusDays((long)i*dias).plusMonths((long)i*mes).plusYears((long) i*anio);
            GestorTareas.getGestorTareas().anadirTarea(tarea.getNombreTarea(),fechaNuevoInicio,nuevafechaFin,tarea.getDescripcion(),tarea.getSitio(),tarea.getHoraInicio(),tarea.getHoraFin(),tarea.getFrecuencia(), tarea.getIdTarea(),tarea.getEtiqueta());
        }
    }

    @FXML
    private void cancelarTodo(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }
}