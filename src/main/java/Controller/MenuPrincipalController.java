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
import java.util.*;

import static javafx.scene.paint.Color.web;

public class MenuPrincipalController {

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Guardamos la fecha que se muestra por pantalla
    LocalDate fechaSeleccionada=LocalDate.now();
    @FXML
    private GridPane calendarioMensual;

    @FXML
    private GridPane calendarioSemanal;

    //Es la matriz que rellena el calendario que le toque
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

   //Se encarga de guardar los nombres de la semana segun el idioma
   private String[] rellenarSemanaSegunIdioma(){
        String[] semanas = new String[7];
        //Se mira el idioma guardado
       Locale idioma=Locale.of(gestorTareas.getIdioma().getCodigo());
       int i=0;
       for(DayOfWeek dayOfWeek : DayOfWeek.values()){
           //Se obtienen los nombres
           semanas[i]= dayOfWeek.getDisplayName(TextStyle.FULL,idioma);
           i++;
       }
       return semanas;
   }
   //Guarda si se muestra en modo mensual o modo semanal
   private String modo="M";

   @FXML
   private ChoiceBox seleccionModo;

   @FXML
   public void initialize() {
       //Rellena el cuadrado de escoger con modo mensual y semanal
       seleccionModo.getItems().addAll("Mensual", "Semanal");
       seleccionModo.setValue("Mensual"); //Se establece el modo mensual al arrancar
       //Se inicia el listener por si se cambia el modo
       seleccionModo.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
           if(valorNuevo.equals("Mensual")){
               modo="M";
               mostrarCalendario();
           }
           else if(valorNuevo.equals("Semanal")){
               modo="S";
               mostrarCalendario();}
       });
       //Inicia el gestor, es decir el model
       gestorTareas.iniciarGestor();

       //Se dan los valores al comboBox de etiquetas
       comboFiltroEtiquetas.setItems(FXCollections.observableArrayList(gestorTareas.getListaEtiquetas()));
       //Se inician los valores de las matrices de ambos modos
       iniciarMatrizVBoxMensual();
       iniciarMatrizVBoxSemanal();
       semana=rellenarSemanaSegunIdioma();

       mostrarCalendario();
       //Muestra los textos de tareas urgentes hoy y mañana
       TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
       TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());
       comboFiltroEtiquetas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           mostrarCalendario();
       });
   }
   //Inicia los VBox para el modo semana
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

    //Muestra las tareas en el menu de la derecha
    private void mostrarTareas(){
        textFecha.setText(fechaSeleccionada.toString());
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        //Se cogen todas las tareas
        List<Tarea> listaTareasMostrar=gestorTareas.getTodasTareas().stream().filter(tarea -> fechaSeleccionada.equals(tarea.getFechaFin())).toList();

        //En caso de que no se escoga etiqueta
        if(etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta")) {
            listaTareasMostrar=listaTareasMostrar.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        VBox vBox=new VBox();
        vBox.setSpacing(5);
        int i=1;
        //Por cada tarea se escribe
        for (Tarea tarea : listaTareasMostrar) {
            Label text = new Label(i+": "+tarea.mostrarTarea());
            text.setFont(Font.font(15));
            text.setCursor(Cursor.HAND);
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
                   e.printStackTrace();
               }
           });
        }
        mostradorTareas.setContent(vBox);
    }

    //Pone la primera letra en mayusculas
    private String capitaze(String frase){
       if(frase==null||frase.isEmpty()) return "";
       else return frase.substring(0,1).toUpperCase()+frase.substring(1);
    }

    //Metodo que muestra el calendario
    public void mostrarCalendario(){
        // Para borrar lo que hay escrito en el calendario
        calendarioMensual.getChildren().clear();
        calendarioSemanal.getChildren().clear();

        //Se ponen los textos, por si se añade alguna tarea para hoy
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());

        //Se pone los meses en el diioma elegido
        if(GestorTareas.getGestorTareas().getIdioma()!=null) cartelMes.setText(" " + capitaze(fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, new Locale(GestorTareas.getGestorTareas().getIdioma().getCodigo(), "ES"))));
        else cartelMes.setText(" "+fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL,new Locale("es","ES")));

        cartelAño.setText("" + fechaSeleccionada.getYear()); //Se pone el mes

        if(modo.equals("M")) mostrarCalendarioMensual(); //Dependiendo del modo mensual o semanal
        else if (modo.equals("S")) mostrarCalendarioSemanal();

        mostrarEtiquetasClasificaciones();
    }
    public void mostrarCalendarioSemanal(){
       //En caso de la semana se hace invisible el mensual y visible el semanal
        calendarioMensual.setVisible(false);
        calendarioSemanal.setVisible(true);

        //Se coge el dia de la semana de la fecha seleccionada, y el lunes de la semana
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);

        LocalDate fechaHoy=LocalDate.now();
        for(int i=0;i<calendarioSemanal.getRowCount();i++){
            for (int j=0;j<calendarioSemanal.getColumnCount();j++){
                //Por cada casilla se pone el VBox correspondiente de la matriz
                VBox casillaActual=calendarioVBoxSemanal[j][i];
                calendarioSemanal.add(casillaActual,j,i);
                //Se limpia la casilla por si acaso
                casillaActual.getChildren().clear();
                //Si es la fila de arriba se pone el dia de la semana
                if(i==0) casillaActual.getChildren().add(new Label(semana[j]));
                else{
                    //Si es la de abajo se pone el numero
                    LocalDate diaQueTocaDibujar = lunesDeEstaSemana.plusDays(j);
                    Label label=new Label(diaQueTocaDibujar.getDayOfMonth() + "");
                    casillaActual.getChildren().add(label);
                    int diaClicado = diaQueTocaDibujar.getDayOfMonth();
                    casillaActual.setOnMouseClicked(event -> {
                        tratarEventoClick(event,diaClicado);
                    });
                    //Si coincide con el dia de hoy se marca
                    if(diaClicado==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear()) label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
                }
            }
        }
        mostrarEtiquetasSemanales();
    }

    public void mostrarCalendarioMensual(){
       //Se pone invisible el semanal y visible el mensual
        calendarioSemanal.setVisible(false);
        calendarioMensual.setVisible(true);
        // Se obtiene la cantidad de días del mes
        int numDiasMes = fechaSeleccionada.lengthOfMonth();
        // Para ver qué día empieza el mes
        int fechaPrimerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();

        LocalDate fechaHoy=LocalDate.now();
        int numMes = 1;
        //Se recorren todas las posiciones, metiendo el VBox de la matriz creado
        for(int i=0; i<calendarioMensual.getRowCount(); i++){
            for (int j=0; j< calendarioMensual.getColumnCount(); j++){
                VBox casillaActual = calendarioVBoxMensual[j][i];
                calendarioMensual.add(casillaActual, j, i);
                casillaActual.getChildren().clear();
                //Si es la fila de arriba solo se pone le nombre del dia de la semana
                if(i == 0)  casillaActual.getChildren().add(new Label(semana[j]));
                else{
                    if(i == 1){
                        if(j >= fechaPrimerDiaMes - 1){
                            //Si es la segunda fila, hay que tener en cuenta que dia comienza el mes
                            Label label=new Label(numMes+"");
                            casillaActual.getChildren().add(label);
                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                                tratarEventoClick(event,diaClicado);
                            });
                            //Si coincide con el dia de hoy se marca
                            if(numMes==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear()) label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
                            numMes++;
                        }
                    }else{
                        //El resto de dias, mientras no se pasen del mes se ponen
                        if(numMes <= numDiasMes){
                            Label label=new Label(numMes+"");
                            casillaActual.getChildren().add(label);
                            int diaClicado = numMes;
                            casillaActual.setOnMouseClicked(event -> {
                               tratarEventoClick(event,diaClicado);
                            });
                            //Se mira si coincide con el dia de hoy
                            if(numMes==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear())  label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
                            numMes++;
                        }
                    }
                }
            }
        }
        mostrarEtiquetasMensuales();
    }

    //Se encarga de tratar si se clica en un dia
    private void tratarEventoClick(MouseEvent event,int diaClicado){
       //Un click mustra las tareas
        if(event.getClickCount()==1){
            fechaSeleccionada = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado);
            mostrarTareas();
            //Dos clicks crea una nueva tarea con la fecha de ese dia
        }else if(event.getClickCount()==2){
            añadirEvento(LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonth(), diaClicado));
        }
    }

    //Para mostrar las tareas de esa semana
    private void mostrarEtiquetasSemanales(){
       //Se cogen todas las tareas
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        Etiqueta etiqueta=(Etiqueta) comboFiltroEtiquetas.getValue();
        if(etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta")) {//Si tiene un filtro se filtra la lista a mostrar
            listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();
        }
        //Se usa como limite de tareas a mostrar en un dia
        int [] maxEtiquetasCalendario=new int[8];
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);

        for(Tarea tarea:listaTareas){
            //Obtenemos la tareas y sus posiciones de donde va
            String titulo = tarea.getNombreTarea();
            LocalDate fecha=tarea.getFechaFin();
            int columna = fecha.getDayOfWeek().getValue() - 1;
            //En caso de que ese dia tenga menos de 8 etiquetas se pone una mas
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
        if(etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta")) {
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

    //El boton para retroceder en el calendario, depende del modo que muestra
    @FXML
    private void retrocederMes(){
        if(modo.equals("M")) fechaSeleccionada=fechaSeleccionada.minusMonths(1);
        else if (modo.equals("S")) fechaSeleccionada=fechaSeleccionada.minusWeeks(1);
        mostrarCalendario();
    }

    //Boton para avanzar en el calendario, depende del modo que muestra
    @FXML
    private void pasarMes(){
       if(modo.equals("M")) fechaSeleccionada=fechaSeleccionada.plusMonths(1);
       else if (modo.equals("S")) fechaSeleccionada=fechaSeleccionada.plusWeeks(1);
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
            mostrarTareas();
        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private Text TareasPendientesHoy;

    @FXML
    private Text TareasPendientesMañana;

    private void mostrarEtiquetasClasificaciones(){
        //Para mostrar las etiquetas
        vBoxEtiquetas.getChildren().clear();
        List<Etiqueta> listaEtiquetas=gestorTareas.getListaEtiquetas().stream().filter(etiqueta -> etiqueta.getNombreEtiqueta() != null).filter(etiqueta -> !etiqueta.getNombreEtiqueta().trim().equalsIgnoreCase(gestorTareas.getEtiquetaNeutra().getNombreEtiqueta().trim())).toList();
        int i=0;
        for(Etiqueta etiqueta : listaEtiquetas){
            //Se muestran todas menos la etiqueta neutra o sin etiqueta
            if(!Objects.equals(etiqueta.getNombreEtiqueta(), "Sin Etiqueta")){
            vBoxEtiquetas.add(new Label(etiqueta.getNombreEtiqueta()),1,i);
                String colorHex = etiqueta.getCodColor();
                javafx.scene.paint.Color colorFinal;
                try {
                    colorFinal = web(colorHex);
                } catch (Exception e) {
                    colorFinal = javafx.scene.paint.Color.web("#808080"); // Color gris de emergencia
                }
                Rectangle cuadradito = new Rectangle(12, 12, colorFinal);

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
    //Se encarga de borrar la etiqeuta que se clica
    private void borrarEtiqueta(Etiqueta etiqueta){
        ResourceBundle bundle = GestorTareas.getGestorTareas().obtenerDiccionario();
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("borrarEtiqueta.Titulo"));
        alert.setHeaderText(bundle.getString("borrarEtiqueta.Header1") + etiqueta.getNombreEtiqueta() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            gestorTareas.eliminarEtiqueta(etiqueta);
            mostrarEtiquetasClasificaciones();
            mostrarCalendario();
            mostrarTareas();
        }
    }

    @FXML
    private GridPane vBoxEtiquetas;

    //Si se da al boton de crear etiqueta
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
}