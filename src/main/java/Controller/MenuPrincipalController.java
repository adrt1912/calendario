package Controller;

import Model.*;
import Utils.CalendarioRender;
import Utils.TareaVisualizer;
import View.view;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import javafx.util.Duration;
import java.util.prefs.Preferences;


public class MenuPrincipalController {

    private final GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Guardamos la fecha que se muestra por pantalla
    private LocalDate fechaSeleccionada=LocalDate.now();
    @FXML
    private GridPane calendarioMensual;

    @FXML
    private Label TareasPendientesHoy;

    @FXML
    private Label TareasPendientesManiana;

    @FXML
    private GridPane vBoxEtiquetas;

    @FXML
    private VBox contenedorSemanal;

    @FXML
    private VBox contenedorDiario;

    //Es la matriz que rellena el calendario que le toque
    private VBox[][] calendarioVBoxMensual =new VBox[7][7];
    private final Pane[] panelesDiasSemanales = new Pane[7];
    private final VBox[] panelesTareasTodoDia=new VBox[7];
    private final VBox panelTareasTodoDiaDiario=new VBox();
    @FXML
    private ScrollPane mostradorTareas;

    @FXML
    private Text cartelAnio;

    @FXML
    private Text cartelMes;

    @FXML
    private TextField buscadorTareas;

    @FXML
    private Text textFecha;

    @FXML
    private ComboBox<Etiqueta> comboFiltroEtiquetas;

    @FXML
    private AnchorPane rootPane;

   //Guarda si se muestra en modo mensual o modo semanal
    private String modo="M";

    @FXML
    private ChoiceBox<String> seleccionModo;

    public void setFechaSeleccionada(LocalDate fechaSeleccionada){this.fechaSeleccionada=fechaSeleccionada;}

    @FXML
    public void initialize() {
       //Rellena el cuadrado de escoger con modo mensual y semanal
       seleccionModo.getItems().addAll("Mensual", "Semanal","Diario");
       seleccionModo.setValue("Mensual"); //Se establece el modo mensual al arrancar
       //Se inicia el listener por si se cambia el modo
       seleccionModo.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
           switch (valorNuevo) {
               case "Mensual" -> modo = "M";
               case "Semanal" -> modo = "S";
               case "Diario" -> modo = "D";
           }
           mostrarCalendario();
       });
       //Inicia el gestor, es decir el model
       gestorTareas.iniciarGestor();

       //Se dan los valores al comboBox de etiquetas
       comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
       //Se inician los valores de las matrices de ambos modos
       calendarioVBoxMensual= CalendarioRender.getCalendarioRender().iniciarMatrizVBoxMensual();
       CalendarioRender.getCalendarioRender().rellenarSemanaSegunIdioma();

       mostrarCalendario();
       //Muestra los textos de tareas urgentes hoy y mañana
       TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
       TareasPendientesManiana.setText(gestorTareas.mostrarTareasUrgentesManiana());
       comboFiltroEtiquetas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> mostrarCalendario());
       buscadorTareas.textProperty().addListener((observable, oldValue, newValue) -> {
           mostrarCalendario();
           mostrarTareas();
       });
       //Para cambiar de mes con las flechas
       Platform.runLater(() -> {
           rootPane.requestFocus();
           if (rootPane.getScene() != null) {
               rootPane.getScene().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                   if (buscadorTareas.isFocused()) return;
                   if (event.getCode() == KeyCode.LEFT) {
                       retrocederMes();
                       event.consume();// Flecha izquierda = Atrás
                   } else if (event.getCode() == KeyCode.RIGHT) {
                       pasarMes();
                       event.consume();// Flecha derecha = Adelante
                   }
               });
           }
       });
   }

    //Pone la primera letra en mayusculas
    private String capitaze(String frase){
        if(frase==null||frase.isEmpty()) return "";
        else return frase.substring(0,1).toUpperCase()+frase.substring(1);
    }

    public void mostrarCalendario(){
        //Para guardar de forma global si esta en modo oscuro o no
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false)) {
        if (!rootPane.getStyleClass().contains("dark-mode")) rootPane.getStyleClass().add("dark-mode");
        } else rootPane.getStyleClass().remove("dark-mode");

        //Se ponen los textos, por si se añade alguna tarea para hoy
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesManiana.setText(gestorTareas.mostrarTareasUrgentesManiana());

        //Se pone los meses en el idioms elegido
        Locale localeSesion = Locale.getDefault();
        String nombreMesTraducido = fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, localeSesion);

        cartelMes.setText(" " + capitaze(nombreMesTraducido));

        cartelMes.setFont(Font.font(50));
        cartelAnio.setFont(Font.font(50));
        //Se pone el año en el cartel
        cartelAnio.setText("" + fechaSeleccionada.getYear());

        //Dependiendo del moto se carga el calendario de esa forma
        if(Objects.equals(modo, "M")){
            CalendarioRender.getCalendarioRender().mostrarCalendarioMensual(calendarioMensual,contenedorSemanal,contenedorDiario,fechaSeleccionada,this,calendarioVBoxMensual);
            TareaVisualizer.getTareaVisualizer().mostrarEtiquetasMensuales(comboFiltroEtiquetas.getValue(),buscadorTareas.getText(),fechaSeleccionada,calendarioVBoxMensual,this);
        }
        else if(Objects.equals(modo, "S")){
            CalendarioRender.getCalendarioRender().mostrarCalendarioSemanal(calendarioMensual,contenedorSemanal,contenedorDiario,fechaSeleccionada,this,panelesDiasSemanales,panelesTareasTodoDia);
            TareaVisualizer.getTareaVisualizer().mostrarEtiquetasSemanales(panelesDiasSemanales, panelesTareasTodoDia, buscadorTareas.getText(), comboFiltroEtiquetas.getValue(), fechaSeleccionada, this);
        }
        else if(Objects.equals(modo, "D")) {
            Pane panelDiaro = CalendarioRender.getCalendarioRender().mostrarCalendarioDiario(calendarioMensual, contenedorSemanal, contenedorDiario, fechaSeleccionada, this, panelTareasTodoDiaDiario);
            TareaVisualizer.getTareaVisualizer().mostarEtiquetasDiarias(panelDiaro, panelTareasTodoDiaDiario, buscadorTareas.getText(), comboFiltroEtiquetas.getValue(), fechaSeleccionada, this);
        }

        //Para que no salte tan brusco la pasar
        javafx.scene.Node panelAnimado = modo.equals("M") ? calendarioMensual : contenedorSemanal;
        FadeTransition transicion = new FadeTransition(Duration.millis(200), panelAnimado);
        transicion.setFromValue(0.3);
        transicion.setToValue(1.0);
        transicion.play();

        // Finalmente pintamos las etiquetas laterales
        TareaVisualizer.getTareaVisualizer().mostrarEtiquetasClasificaciones(vBoxEtiquetas, this);
    }

    //Muestra las tareas en el menu de la derecha
    public void mostrarTareas(){
        textFecha.setText(fechaSeleccionada.toString());
        Etiqueta etiqueta= comboFiltroEtiquetas.getValue();
        //Se cogen todas las tareas
        List<Tarea> listaTareasMostrar=gestorTareas.getTodasTareas().stream().filter(tarea -> fechaSeleccionada.equals(tarea.getFechaFin())).toList();

        //En caso de que no se escoga etiqueta
        if(etiqueta != null && !etiqueta.nombreEtiqueta().equals("Sin Etiqueta")) listaTareasMostrar=listaTareasMostrar.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();

        String textoBusqueda = buscadorTareas.getText() != null ? buscadorTareas.getText().toLowerCase() : "";
        if(!textoBusqueda.isEmpty()) listaTareasMostrar = listaTareasMostrar.stream().filter(tarea -> tarea.getNombreTarea().toLowerCase().contains(textoBusqueda)).toList();

        VBox vBox=new VBox();
        vBox.setSpacing(5);
        int i=1;
        //Por cada tarea se escribe
        for (Tarea tarea : listaTareasMostrar) {
            Label text = new Label(i+": "+tarea.mostrarTarea()+"\n\n\n");
            text.setFont(Font.font(15));
            text.setCursor(Cursor.HAND);
            text.setWrapText(true);
            text.maxWidthProperty().bind(mostradorTareas.widthProperty().subtract(20));
           vBox.getChildren().add(text);
           i++;

           //Por si se clica en la tarea se abre el menu de edicion
           text.setOnMouseClicked(event -> {
               try{
                   view.showTareaVentana(tarea);
                   mostrarCalendario();
                   mostrarTareas();
               } catch (Exception e) {
                   System.err.println("Error al abrir la ventana: " + e.getMessage());
               }
           });
        }
        mostradorTareas.setContent(vBox);
    }

    //Se encarga de tratar si se clica en un dia
    public void tratarEventoClick(MouseEvent event, LocalDate fechaSeleccionada, LocalTime horaClick){
       //Un click mustra las tareas
        if(event.getClickCount()==1){
            this.fechaSeleccionada = fechaSeleccionada;
            mostrarTareas();
            //Dos clicks crea una nueva tarea con la fecha de ese dia
        }else if(event.getClickCount()==2) aniadirEvento(fechaSeleccionada,horaClick);
    }

    //Para eliminar un etiqueta
    public void borrarEtiqueta(Etiqueta etiqueta){
        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        //Salta una ventan de confirmacion
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("borrarEtiqueta.Titulo"));
        alert.setHeaderText(bundle.getString("borrarEtiqueta.Header1") + etiqueta.nombreEtiqueta() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            gestorTareas.eliminarEtiqueta(etiqueta);
            TareaVisualizer.getTareaVisualizer().mostrarEtiquetasClasificaciones(vBoxEtiquetas,this);
            comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
            mostrarCalendario();
            mostrarTareas();
        }
    }

    //El boton para retroceder en el calendario, depende del modo que muestra
    @FXML
    private void retrocederMes(){
        switch (modo) {
            case "M" -> fechaSeleccionada = fechaSeleccionada.minusMonths(1);
            case "S" -> fechaSeleccionada = fechaSeleccionada.minusWeeks(1);
            case "D" -> fechaSeleccionada = fechaSeleccionada.minusDays(1);
        }
        mostrarCalendario();
    }

    //Boton para avanzar en el calendario, depende del modo que muestra
    @FXML
    private void pasarMes(){
        switch (modo) {
            case "M" -> fechaSeleccionada = fechaSeleccionada.plusMonths(1);
            case "S" -> fechaSeleccionada = fechaSeleccionada.plusWeeks(1);
            case "D" -> fechaSeleccionada = fechaSeleccionada.plusDays(1);
        }
        mostrarCalendario();
    }

    //Es el metodo de fxml, al dar al boton, como no hay fecha establecida se pone anull y ya se asignara
    @FXML
    public void aniadirEvento(){
        aniadirEvento(null,null);
    }

    //La fecha se pasa por si se inicia con doble click, se ponga automaticamente
    @FXML
    private void aniadirEvento(LocalDate fecha, LocalTime horaInicio) {
        try {
            view.showCrearTArea(fecha,horaInicio);
            mostrarCalendario();
            mostrarTareas();
        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
        }
    }

    //Si se da al boton de crear etiqueta
    @FXML
    private void nuevaEtiqueta(){
        try {
            view.showNuevaEtiqueta();
            TareaVisualizer.getTareaVisualizer().mostrarEtiquetasClasificaciones(vBoxEtiquetas,this);

            comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Si se da al boton de configuracion
    @FXML
    private void abrirConfiguracion(){
        try {
            view.showConfiguracionMenu();
            mostrarCalendario();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Boton para volver al dia de hoy
    @FXML
    private void volverHoy(){
        fechaSeleccionada=LocalDate.now();
        mostrarCalendario();
    }

    //Al escribir algo, se actualzia la vista del calendario
    @FXML
    private void buscarTarea(){
        mostrarCalendario();
    }

    //Para arrastrar, cambia su fecha inicial
    public void moverTareaA(String idTarea, LocalDate fechaDestino){

       Tarea tarea=gestorTareas.getTodasTareas().stream().filter(tarea1 -> tarea1.getIdTarea().equals(idTarea)).findFirst().orElse(null);
       if(tarea!=null){
           long diasDuracion = java.time.temporal.ChronoUnit.DAYS.between(tarea.getFechaInicio(), tarea.getFechaFin());
           LocalDate fechafin=fechaDestino.plusDays(diasDuracion);
           gestorTareas.modificarTarea(tarea,tarea.getNombreTarea(),fechaDestino,fechafin,tarea.getDescripcion(),tarea.getSitio(),tarea.getHoraInicio(),tarea.getHoraFin(),tarea.getFrecuencia(),tarea.getEstadoTarea(),tarea.getEtiqueta());
       }
       mostrarCalendario();
    }
}