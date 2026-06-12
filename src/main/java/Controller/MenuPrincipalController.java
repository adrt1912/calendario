package Controller;

import Model.*;
import View.view;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static javafx.scene.paint.Color.web;

public class MenuPrincipalController {

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Guardamos la fecha que se muestra por pantalla
    LocalDate fechaSeleccionada=LocalDate.now();
    @FXML
    private GridPane calendario;

    private VBox[][] calendarioVBox=new VBox[7][7];

    @FXML
    private ScrollPane mostradorTareas;

    @FXML
    private Text cartelAño;

    @FXML
    private Text cartelMes;

    @FXML
    private Text textFecha;


    @FXML
    private ComboBox comboFiltroEtiquetas;

    //Para escribir el titulo
   private final String[] semana= {"Lunes","Martes","Miercoles","Jueves","Viernes","Sábado","Domingo"};
   // private String[] mes={"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
   @FXML
   public void initialize() {
       gestorTareas.iniciarGestor();
       comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
       iniciarMatrizVBox();
       mostrarCalendario();
       TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
       TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());
       comboFiltroEtiquetas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           mostrarCalendario();
       });
       
   }

    private void iniciarMatrizVBox(){
       //Simplemente crea un VBox en cada hueco del calendario, para poder añadir ahi todos los nombres de las tareas
        for(int i=0;i<7;i++){
            for (int j=0;j<7;j++){
                calendarioVBox[j][i]=new VBox();
            }
        }
    }

    private void mostrarTareas(){
        textFecha.setText(fechaSeleccionada.toString());
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        List<Tarea> listaTareasMostrar=gestorTareas.getTodasTareas().stream().filter(tarea -> fechaSeleccionada.equals(tarea.getFechaFin())).toList();

        if(etiqueta!=null) {
            listaTareasMostrar=listaTareasMostrar.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        VBox vBox=new VBox();
        vBox.setSpacing(5);
        int i=1;
        for (Tarea tarea : listaTareasMostrar) {
            Label text = new Label(i+": "+tarea.mostrarTarea());
            text.setFont(Font.font(15));
            text.setCursor(Cursor.HAND);
           vBox.getChildren().add(text);
           i++;

           text.setOnMouseClicked(event -> {
               try{
                   view.showTareaVentana(tarea);
                   mostrarCalendario();
                   mostrarTareas();
               } catch (Exception ignored) {
                   throw new RuntimeException(ignored);

               }
           });
        }
        mostradorTareas.setContent(vBox);
    }
    public void mostrarCalendario(){

        // Para borrar lo que hay escrito en el calendario
        calendario.getChildren().clear();
        iniciarMatrizVBox();
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());

        // Se obtiene la cantidad de días del mes
        int numDiasMes = fechaSeleccionada.lengthOfMonth();
        // Para ver qué día empieza el mes
        int fechaPrimerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();

        cartelMes.setText(" " + fechaSeleccionada.getMonth().name());
        cartelAño.setText("" + fechaSeleccionada.getYear());

        int numMes = 1;
        for(int i=0; i<calendario.getRowCount(); i++){
            for (int j=0; j< calendario.getColumnCount(); j++){
                VBox casillaActual = calendarioVBox[j][i];
                calendario.add(casillaActual, j, i);

                if(i == 0){
                    // Fila de cabecera (Lunes, Martes...) -> No hace nada al hacer clic
                    casillaActual.getChildren().add(new Label(semana[j]));
                }
                else{
                    if(i == 1){
                        if(j >= fechaPrimerDiaMes - 1){
                            casillaActual.getChildren().add(new Label(numMes + ""));

                            // ¡AQUÍ ESTÁ EL TRUCO SEGURO!: Guardamos el día real en una variable final
                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                                tratarEventoClick(event,diaClicado);
                            });

                            numMes++;
                        }
                    }else{
                        if(numMes <= numDiasMes){
                            casillaActual.getChildren().add(new Label(numMes + ""));

                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                               tratarEventoClick(event,diaClicado);
                            });
                            numMes++;
                        }
                    }
                }
            }
        }
        mostrarEtiquetas();
        mostrarEtiquetasClasificaciones();
    }

    private void tratarEventoClick(MouseEvent event,int diaClicado){

        if(event.getClickCount()==1){
            fechaSeleccionada = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado);
            mostrarTareas();
        }else if(event.getClickCount()==2){
            añadirEvento(LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado));
        }

    }

    private void mostrarEtiquetas(){
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        if(etiqueta!=null) {
            listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        int primerDiaMes=LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        for (Tarea tarea : listaTareas) {
            LocalDate fecha = tarea.getFechaFin();
            if (fecha.getMonth().equals(fechaSeleccionada.getMonth())&&fecha.getYear()==fechaSeleccionada.getYear()) {
                String titulo=tarea.getNombreTarea();
                int pos=(fecha.getDayOfMonth()-2)+primerDiaMes;
                int columna=pos%7;
                int fila=(pos/7)+1;
                Label label=new Label((titulo));

                if(tarea.getEstadoTarea()!= EstadoTarea.EN_PROCESO){
                    label.setOpacity(0.2);
                }else{
                    label.setOpacity(1);}

                    if(!Objects.equals(tarea.getEtiqueta().getNombreEtiqueta(), "Sin Etiqueta")) {
                        String colorHex = tarea.getEtiqueta().getCodColor();

                        label.setStyle(
                                "-fx-background-color: " + colorHex + ";" +
                                        "-fx-border-color: derive(" + colorHex + ", -60%);" +
                                        "-fx-border-width: 2px;" +
                                        "-fx-border-radius: 5px;" +                           // Esquinas del borde redondeadas
                                        "-fx-background-radius: 5px;" +                        // Esquinas del fondo idénticas para que encajen
                                        "-fx-text-fill: white;" +                             // Texto blanco para que contraste con el fondo relleno
                                        "-fx-font-weight: bold;"                         // Texto en negrita para que se lea mejor
                                           );
                    }else{
                        label.setStyle(
                                "-fx-background-color: #f4f4f4;" +
                                        "-fx-border-color: #444444;" +
                                        "-fx-border-width: 1.5px;" +
                                        "-fx-border-radius: 5px;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-text-fill: #333333;" );
                    }
                calendarioVBox[columna][fila].getChildren().add(label);
            }
        }
    }

    @FXML
    private void retrocederMes(){
        fechaSeleccionada=fechaSeleccionada.minusMonths(1);
        mostrarCalendario();
    }

    @FXML
    private void pasarMes(){
        fechaSeleccionada=fechaSeleccionada.plusMonths(1);
        mostrarCalendario();
    }

    //Es el metodo de fxml, al dar al boton, como no hay fecha establecida se pone anull y ya se asignara
    @FXML
    public void añadirEvento(){
        añadirEvento(null);
    }

    //La fecha se pasa por si se inicia con doble click, se ponga automaticamente
    @FXML
    private void añadirEvento(LocalDate fecha) {
        try {
            view.showCrearTArea(fecha);
            mostrarCalendario();
        } catch (Exception ignored) {}
    }

    @FXML
    private Text TareasPendientesHoy;

    @FXML
    private Text TareasPendientesMañana;

    private void mostrarEtiquetasClasificaciones(){
        vBoxEtiquetas.getChildren().clear();
        List<Etiqueta> listaEtiquetas=gestorTareas.getListaEtiquetas();
        int i=0;
        for(Etiqueta etiqueta : listaEtiquetas){
            if(etiqueta.getNombreEtiqueta()!="Sin Etiqueta"){
            vBoxEtiquetas.add(new Label(etiqueta.getNombreEtiqueta()),1,i);
           Rectangle cuadradito = new Rectangle(12, 12, web(etiqueta.getCodColor()));
            vBoxEtiquetas.add(cuadradito,0,i);
            Button button= new Button("🗑");
            button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-text-fill: #ff4444; -fx-cursor: hand;");
            button.setOnMouseClicked(event -> {
                borrarEtiqueta(etiqueta);
                comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
            });
            vBoxEtiquetas.add(button,2,i);
            i++;
        }
    }

    }
    private void borrarEtiqueta(Etiqueta etiqueta){
        List<Tarea> listaTareasAfectadas=gestorTareas.getTodasTareas().stream().filter(tarea ->tarea.getEtiqueta()!=null&& tarea.getEtiqueta().getNombreEtiqueta().equals(etiqueta.getNombreEtiqueta())).toList();
        for (Tarea listaTareasAfectada : listaTareasAfectadas) {
            listaTareasAfectada.setEtiqueta(null);
        }
        gestorTareas.eliminarEtiqueta(etiqueta);
        mostrarEtiquetasClasificaciones();
        mostrarCalendario();
    }

    @FXML
    private GridPane vBoxEtiquetas;

    @FXML
    private void nuevaEtiqueta(){

        try {
            view.showNuevaEtiqueta();
            mostrarEtiquetasClasificaciones();
            comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void abrirConfiguracion(){
        try {
            view.showConfiguracionMenu();
            mostrarCalendario();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}