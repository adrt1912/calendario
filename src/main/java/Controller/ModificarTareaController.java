package Controller;

import Model.*;
import View.view;
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

//Menu para modificar tareas
public class ModificarTareaController {
    //todos los campos que hay, ademas de guardar la tarea a modificar
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
    private AnchorPane rootPane;

    public void initialize(){
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ConfiguracionController.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

//Una vez recibe la tarea establece los campos con los valores que tenemos
public void setTareaMos(Tarea tareaMos) {
        this.tareaMos = tareaMos;
        campoTitulo.setText(tareaMos.getNombreTarea());
        if(tareaMos.getFechaFin()!=null){
            campoFecha.setValue(tareaMos.getFechaFin());
        }
        if(tareaMos.getHora()!=null){
            campoHora.setText(GestorTareas.getGestorTareas().obtenerHoraFormateada(tareaMos.getHora()));
        }
        if(tareaMos.getSitio()!=null){
            campoSitio.setText(tareaMos.getSitio());
        }
        if(tareaMos.getDescripcion()!=null){
            campoDescripcion.setText(tareaMos.getDescripcion());
        }
        if(tareaMos.getEstadoTarea().equals(EstadoTarea.COMPLETADA)){
            checkCompletada.setSelected(true);
        }
        //Establece el valor en los combobox ademas de rellenarlos
    campoPerioricidad.getItems().addAll(Periodicidad.values());
    boxEtiquetas.getItems().addAll(GestorTareas.getGestorTareas().getListaEtiquetas());
    textoTituloTop.setText(tareaMos.getNombreTarea());
    campoPerioricidad.setValue(tareaMos.getFrecuencia());
    boxEtiquetas.setValue(tareaMos.getEtiqueta());
}
//Si se elige completado cambia su estado
@FXML
private void cambiarEstado(){
    if(checkCompletada.isSelected()){
        tareaMos.setEstadoTarea(EstadoTarea.COMPLETADA);
    }else {
        tareaMos.setEstadoTarea(EstadoTarea.EN_PROCESO);}
}

@FXML
private Button botonCancelar;

//Boton para cerrar sin guardar
@FXML
private void cerrarTodo(){
    Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
    ventanaActual.close();
}

//Si se pulsa en eliminar tarea se borra
@FXML
private void eliminarTarea(){//En caso de qeu sea periodica sale una pantalla emergente
    if(tareaMos.getFrecuencia()!=Periodicidad.NUNCA){
        try{
            view.showConfirmacionEl(tareaMos);
        } catch (Exception e) {
            throw new RuntimeException(e) ;
        }
    }else {
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

//Guarda la nueva modificacion y cierra
@FXML
private void modificarTarea() {

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
        // Primero intentamos leerlo normal (formato 24h, ej: "18:30")
        time = LocalTime.parse(horaText);
    } catch (Exception e) {
        try {
            // Si falla, intentamos leerlo en formato 12h (ej: "06:30 PM")
            DateTimeFormatter formato12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            time = LocalTime.parse(horaText, formato12h);
        } catch (Exception ex) {
            // Si el usuario ha escrito "patata", entonces sí lo dejamos en null
            time = null;
        }
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