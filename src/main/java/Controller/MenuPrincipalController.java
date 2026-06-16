package Controller;

import Model.*;
import View.view;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static javafx.scene.paint.Color.web;

public class MenuPrincipalController {

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Guardamos la fecha que se muestra por pantalla
    LocalDate fechaSeleccionada=LocalDate.now();
    @FXML
    private GridPane calendarioMensual;

    @FXML
    private GridPane calendarioSemanal;

    private VBox[][] calendarioVBoxMensual =new VBox[7][7];

    private VBox[][] calendarioVBoxSemanal =new VBox[7][2];

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
   private String[] semana;

   private String[] rellenarSemanaSegunIdioma(){
        String[] semanas = new String[7];
       Locale idioma=Locale.of(gestorTareas.getIdioma().getCodigo());
       int i=0;
       for(DayOfWeek dayOfWeek : DayOfWeek.values()){
           semanas[i]= dayOfWeek.getDisplayName(TextStyle.FULL,idioma);
           i++;
       }
       return semanas;
   }
   private String modo="M";

   @FXML
   private ChoiceBox seleccionModo;

   @FXML
   public void initialize() {
       seleccionModo.getItems().addAll("Mensual", "Semanal");
       seleccionModo.setValue("Mensual");
       seleccionModo.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
           if(valorNuevo.equals("Mensual")) modo="M";
           else if(valorNuevo.equals("Semanal"))modo="S";
       });
       gestorTareas.iniciarGestor();

       comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
       iniciarMatrizVBoxMensual();
       iniciarMatrizVBoxSemanal();

       semana=rellenarSemanaSegunIdioma();

       mostrarCalendario();
       TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
       TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());
       comboFiltroEtiquetas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           mostrarCalendario();
       });
   }
   private void iniciarMatrizVBoxSemanal(){
       for(int i=0;i<2;i++){
           for(int j=0;j<7;j++){
               calendarioVBoxSemanal[j][i]=new VBox();
           }
       }
   }

    private void iniciarMatrizVBoxMensual(){
       //Simplemente crea un VBox en cada hueco del calendario, para poder añadir ahi todos los nombres de las tareas
        for(int i=0;i<7;i++){
            for (int j=0;j<7;j++){
                calendarioVBoxMensual[j][i]=new VBox();
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
               } catch (Exception ignored) {}
           });
        }
        mostradorTareas.setContent(vBox);
    }

    public void mostrarCalendario(){

        // Para borrar lo que hay escrito en el calendario
        calendarioMensual.getChildren().clear();
        calendarioSemanal.getChildren().clear();
        iniciarMatrizVBoxMensual();
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());

        if(GestorTareas.getGestorTareas().getIdioma()!=null) cartelMes.setText(" " + fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, new Locale(GestorTareas.getGestorTareas().getIdioma().getCodigo(), "ES")));
        else cartelMes.setText(" "+fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL,new Locale("es","ES")));

        cartelAño.setText("" + fechaSeleccionada.getYear());

        if(modo.equals("M")) mostrarCalendarioMensual();
        else if (modo.equals("S")) mostrarCalendarioSemanal();

        mostrarEtiquetasClasificaciones();
    }
    public void mostrarCalendarioSemanal(){
        calendarioMensual.setVisible(false);
        calendarioSemanal.getChildren().clear();
        calendarioSemanal.setVisible(true);

        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);

        for(int i=0;i<calendarioSemanal.getRowCount();i++){
            for (int j=0;j<calendarioSemanal.getColumnCount();j++){

                VBox casillaActual=calendarioVBoxSemanal[j][i];
                calendarioSemanal.add(casillaActual,j,i);
                casillaActual.getChildren().clear();
                if(i==0) casillaActual.getChildren().add(new Label(semana[j]));
                else{
                    LocalDate diaQueTocaDibujar = lunesDeEstaSemana.plusDays(j);
                    casillaActual.getChildren().add(new Label(diaQueTocaDibujar.getDayOfMonth() + ""));
                    int diaClicado = diaQueTocaDibujar.getDayOfMonth();
                    casillaActual.setOnMouseClicked(event -> {
                        tratarEventoClick(event,diaClicado);
                    });
                }
            }
        }
        mostrarEtiquetasSemanales();
    }

    public void mostrarCalendarioMensual(){
        if(calendarioSemanal!=null) calendarioSemanal.setVisible(false);
        calendarioMensual.setVisible(true);
        // Se obtiene la cantidad de días del mes
        int numDiasMes = fechaSeleccionada.lengthOfMonth();
        // Para ver qué día empieza el mes
        int fechaPrimerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();

        LocalDate fechaHoy=LocalDate.now();
        int numMes = 1;
        for(int i=0; i<calendarioMensual.getRowCount(); i++){
            for (int j=0; j< calendarioMensual.getColumnCount(); j++){
                VBox casillaActual = calendarioVBoxMensual[j][i];
                calendarioMensual.add(casillaActual, j, i);
                if(i == 0)  casillaActual.getChildren().add(new Label(semana[j]));
                else{
                    if(i == 1){
                        if(j >= fechaPrimerDiaMes - 1){
                            Label label=new Label(numMes+"");
                            casillaActual.getChildren().add(label);

                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                                tratarEventoClick(event,diaClicado);
                            });
                            if(numMes==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear()) label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em;");
                            numMes++;
                        }
                    }else{
                        if(numMes <= numDiasMes){
                            Label label=new Label(numMes+"");
                            casillaActual.getChildren().add(label);
                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                               tratarEventoClick(event,diaClicado);
                            });
                            if(numMes==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear())  label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em;");
                            numMes++;
                        }
                    }
                }
            }
        }
        mostrarEtiquetasMensuales();
    }

    private void tratarEventoClick(MouseEvent event,int diaClicado){

        if(event.getClickCount()==1){
            fechaSeleccionada = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado);
            mostrarTareas();
        }else if(event.getClickCount()==2){
            añadirEvento(LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado));
        }

    }
    private void mostrarEtiquetasSemanales(){
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        if(etiqueta!=null) {
            listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        int [] maxEtiquetasCalendario=new int[8];
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);

        for(Tarea tarea:listaTareas){
            //Obtenemos la tareas y sus posiciones de donde va
            String titulo = tarea.getNombreTarea();
            LocalDate fecha=tarea.getFechaFin();
            int columna = fecha.getDayOfWeek().getValue() - 1;
            //En caso de que ese dia tenga menos de dos etiquetas se pone una mas
            if (maxEtiquetasCalendario[fecha.getDayOfWeek().getValue()] <= 9) {
                //se comprueba que esta en el mismo mes y año que aparece en pantalla
                LocalDate domingoDeEstaSemana = lunesDeEstaSemana.plusDays(6);
                if (!fecha.isBefore(lunesDeEstaSemana) && !fecha.isAfter(domingoDeEstaSemana)) {
                    Label label = new Label((titulo));
                    //Dependiendo del estado se ve mas o menos la etiqueta
                    if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) {
                        label.setOpacity(0.2);
                    } else {
                        label.setOpacity(1);
                    }
                    //Se ven distintos wi tiene etiqueta o no
                    if (!Objects.equals(tarea.getEtiqueta().getNombreEtiqueta(), "Sin Etiqueta")) {
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
                    } else {
                        label.setStyle(
                                "-fx-background-color: #f4f4f4;" +
                                        "-fx-border-color: #444444;" +
                                        "-fx-border-width: 1.5px;" +
                                        "-fx-border-radius: 5px;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-text-fill: #333333;");
                    }
                    //Se pone en el calendario
                    calendarioVBoxSemanal[columna][1].getChildren().add(label);
                }
                //Se mira si llega a las tres etiquetas, si es asi se ponen tres puntos indicando qeu hay mas
            }else if (maxEtiquetasCalendario[fecha.getDayOfWeek().getValue()]==2&&fecha.getMonth().equals(fechaSeleccionada.getMonth()) && fecha.getYear() == fechaSeleccionada.getYear()) calendarioVBoxMensual[columna][1].getChildren().add(new Label("..."));
            maxEtiquetasCalendario[fecha.getDayOfWeek().getValue()]++;
        }
    }

    private void mostrarEtiquetasMensuales(){
       //Obtenemos todas las tareas
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        if(etiqueta!=null) {
            listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        //Obtenemos el primer di del mes
        int primerDiaMes=LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        //Es para evitar que cada dia supere la cantidad maxima de etiquetas
        int [] maxEtiquetasCalendario=new int[32];

        //Un bucle que va poniendo todas las tareas
        for(Tarea tarea:listaTareas){
            //Obtenemos la tareas y sus posiciones de donde va
            String titulo = tarea.getNombreTarea();
            LocalDate fecha=tarea.getFechaFin();
            int pos = (fecha.getDayOfMonth() - 2) + primerDiaMes;
            int columna = pos % 7;
            int fila = (pos / 7) + 1;
            //En caso de que ese dia tenga menos de dos etiquetas se pone una mas
            if (maxEtiquetasCalendario[fecha.getDayOfMonth()] <= 1) {
                //se comprueba que esta en el mismo mes y año que aparece en pantalla
                if (fecha.getMonth().equals(fechaSeleccionada.getMonth()) && fecha.getYear() == fechaSeleccionada.getYear()) {
                    Label label = new Label((titulo));
                    //Dependiendo del estado se ve mas o menos la etiqueta
                    if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) {
                        label.setOpacity(0.2);
                    } else {
                        label.setOpacity(1);
                    }
                    //Se ven distintos wi tiene etiqueta o no
                    if (!Objects.equals(tarea.getEtiqueta().getNombreEtiqueta(), "Sin Etiqueta")) {
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
                    } else {
                        label.setStyle(
                                "-fx-background-color: #f4f4f4;" +
                                        "-fx-border-color: #444444;" +
                                        "-fx-border-width: 1.5px;" +
                                        "-fx-border-radius: 5px;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-text-fill: #333333;");
                    }
                    //Se pone en el calendario
                    calendarioVBoxMensual[columna][fila].getChildren().add(label);
                }
                //Se mira si llega a las tres etiquetas, si es asi se ponen tres puntos indicando qeu hay mas
            }else if (maxEtiquetasCalendario[fecha.getDayOfMonth()]==2&&fecha.getMonth().equals(fechaSeleccionada.getMonth()) && fecha.getYear() == fechaSeleccionada.getYear()) {
                calendarioVBoxMensual[columna][fila].getChildren().add(new Label("..."));
            }
            maxEtiquetasCalendario[fecha.getDayOfMonth()]++;
        }
    }

    @FXML
    private void retrocederMes(){
        if(modo=="M") fechaSeleccionada=fechaSeleccionada.minusMonths(1);
        else if (modo=="S") fechaSeleccionada=fechaSeleccionada.minusWeeks(1);
        mostrarCalendario();
    }

    @FXML
    private void pasarMes(){
       if(modo=="M") fechaSeleccionada=fechaSeleccionada.plusMonths(1);
       else if (modo=="S") fechaSeleccionada=fechaSeleccionada.plusWeeks(1);
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
        List<Etiqueta> listaEtiquetas=gestorTareas.getListaEtiquetas().stream().filter(etiqueta -> etiqueta.getNombreEtiqueta() != null).filter(etiqueta -> !etiqueta.getNombreEtiqueta().trim().equalsIgnoreCase(gestorTareas.getEtiquetaNeutra().getNombreEtiqueta().trim())).toList();
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