package Controller;

import Model.*;
import View.view;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

//Menu para modificar tareas
public class ModificarTareaController {
    //todos los campos que hay, ademas de guardar la tarea a modificar
    private Tarea tareaMos;

    @FXML
    private TextField campoTitulo;

    @FXML
    private DatePicker campoFechaInicio;

    @FXML
    private TextField campoHoraInicio;

    @FXML
    private TextField campoHoraFin;

    @FXML
    private TextField campoSitio;

    @FXML
    private ComboBox<Periodicidad> campoPerioricidad;

    @FXML
    private TextArea campoDescripcion;

    @FXML
    private CheckBox checkCompletada;

    @FXML
    private Label textoTituloTop;

    @FXML
    private ComboBox<Etiqueta> boxEtiquetas;

    @FXML
    private Text textoError;

    @FXML
    private DatePicker campoFechaFin;

    @FXML
    private Button botonCancelar;

    @FXML
    private AnchorPane rootPane;

    //Initialize que pone de color y el idioma guardado
    public void initialize(){
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) rootPane.getStyleClass().add("dark-mode");
    }

//Una vez recibe la tarea establece los campos con los valores que tenemos
public void setTareaMos(Tarea tareaMos) {
        this.tareaMos = tareaMos;
        campoTitulo.setText(tareaMos.getNombreTarea());
        if(tareaMos.getFechaFin()!=null)campoFechaFin.setValue(tareaMos.getFechaFin());

        if(tareaMos.getFechaInicio()!=null)campoFechaInicio.setValue(tareaMos.getFechaInicio());

        if(tareaMos.getHoraInicio()!=null)campoHoraInicio.setText(GestorTareas.getGestorTareas().obtenerHoraFormateada(tareaMos.getHoraInicio()));

        if(tareaMos.getHoraFin()!=null)campoHoraFin.setText(GestorTareas.getGestorTareas().obtenerHoraFormateada(tareaMos.getHoraFin()));

        if(tareaMos.getSitio()!=null)campoSitio.setText(tareaMos.getSitio());

        if(tareaMos.getDescripcion()!=null)campoDescripcion.setText(tareaMos.getDescripcion());

        if(tareaMos.getEstadoTarea().equals(EstadoTarea.COMPLETADA))checkCompletada.setSelected(true);

        //Establece el valor en los combobox ademas de rellenarlos
        campoPerioricidad.setItems(FXCollections.observableArrayList(Periodicidad.values()));
        campoPerioricidad.setValue(tareaMos.getFrecuencia());

        boxEtiquetas.setItems(FXCollections.observableArrayList(GestorTareas.getGestorTareas().getListaEtiquetas()));
        textoTituloTop.setText(tareaMos.getNombreTarea());
        boxEtiquetas.setValue(tareaMos.getEtiqueta());
    }
    //Si se elige completado cambia su estado
    @FXML
    private void cambiarEstado(){
        if(checkCompletada.isSelected())tareaMos.setEstadoTarea(EstadoTarea.COMPLETADA);
        else tareaMos.setEstadoTarea(EstadoTarea.EN_PROCESO);
    }

    //Boton para cerrar sin guardar
    @FXML
    private void cerrarTodo(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

//Si se pulsa en eliminar tarea se borra
@FXML
private void eliminarTarea(){//En caso de que sea periodica sale una pantalla emergente
    if(tareaMos.getFrecuencia()!=Periodicidad.NUNCA){
        try{
            view.showConfirmacionEl(tareaMos);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }else {
        //Sale una pantalla de confirmacion
        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("modificarTarea.borrarTarea.confirmar.Titulo"));
        alert.setHeaderText(bundle.getString("modificarTarea.borrarTarea.confirmar.text1") + tareaMos.getNombreTarea() + "?");
        alert.setContentText(bundle.getString("modificarTarea.borrarTarea.confirmar.text2"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) GestorTareas.getGestorTareas().eliminarTarea(tareaMos);
    }
    cerrarTodo();
}

//Guarda la nueva modificacion y cierra, lee todos los atributos y se los actualiza
@FXML
private void modificarTarea() {

    String titulo = campoTitulo.getText();
    LocalDate fechaInic = campoFechaInicio.getValue();
    if (fechaInic == null) fechaInic = LocalDate.now();

    LocalDate fechaFin=campoFechaFin.getValue();
    if(fechaFin==null) fechaFin=LocalDate.now();

    String descripcion = campoDescripcion.getText();
    String sitio = campoSitio.getText();
    LocalTime time;
    String horaInicio = campoHoraInicio.getText();
    String horaFin = campoHoraFin.getText();

    try {
        // Primero intentamos leerlo normal (formato 24h, ej: "18:30")
        time = LocalTime.parse(horaInicio);
    } catch (Exception e) {
        try {
            // Si falla, intentamos leerlo en formato 12h (ej: "06:30 PM")
            DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            time = LocalTime.parse(horaInicio, formato12h);
        } catch (Exception ex) {
            // Si el usuario ha escrito "patata", entonces sí lo dejamos en null
            time = null;
        }
    }

    LocalTime horaFin1;
    try {
        // Primero intentamos leerlo normal (formato 24h, ej: "18:30")
        horaFin1 = LocalTime.parse(horaFin);
    } catch (Exception e) {
        try {
            // Si falla, intentamos leerlo en formato 12h (ej: "06:30 PM")
            DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            horaFin1 = LocalTime.parse(horaFin, formato12h);
        } catch (Exception ex) {
            // Si el usuario ha escrito "patata", entonces sí lo dejamos en null
            horaFin1 = null;
        }
    }
    Periodicidad frecuencia = campoPerioricidad.getValue();
    if (frecuencia == null) frecuencia = Periodicidad.NUNCA; // Le damos un valor por defecto

    EstadoTarea estado = tareaMos.getEstadoTarea();

    Etiqueta etiqueta = boxEtiquetas.getValue();

    //Obliga a que exista un titulo
    if (titulo.isBlank()) textoError.setText("Introduzca un titulo");
     else {
        GestorTareas.getGestorTareas().modificarTarea(tareaMos, titulo,fechaInic, fechaFin, descripcion, sitio, time, horaFin1,frecuencia, estado, etiqueta);
        cerrarTodo();
    }
}
}