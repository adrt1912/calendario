package Utils;

import Controller.ConfiguracionController;
import Controller.MenuPrincipalController;
import Model.GestorTareas;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CalendarioRender {

    private Preferences prefs; //Para guardar de forma global si esta en modo oscuro o no

    private static CalendarioRender calendarioRender;

    public static CalendarioRender getCalendarioRender() {
        if (calendarioRender == null) {
            calendarioRender = new CalendarioRender();
        }
        return calendarioRender;
    }

    private CalendarioRender(){
        calendarioRender=this;
        prefs = Preferences.userNodeForPackage(ConfiguracionController.class);}

    private GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    private String[] semana;

    public VBox[][] iniciarMatrizVBoxMensual(){
        VBox[][] calendarioVBoxMensual=new VBox[7][7];
        //Simplemente crea un VBox en cada hueco del calendario, para poder añadir ahi todos los nombres de las tareas
        for(int i=0;i<7;i++){
            for (int j=0;j<7;j++){
                calendarioVBoxMensual[j][i]=new VBox();
            }
        }
        return calendarioVBoxMensual;
    }

    public void rellenarSemanaSegunIdioma(){
        String[] semanas = new String[7];
        //Se mira el idioma guardado
        Locale idioma=Locale.of(gestorTareas.getIdioma().getCodigo());
        int i=0;
        for(DayOfWeek dayOfWeek : DayOfWeek.values()){
            //Se obtienen los nombres
            String nombreDia=dayOfWeek.getDisplayName(TextStyle.FULL,idioma);
            semanas[i]=nombreDia.substring(0,1).toUpperCase()+nombreDia.substring(1);
            i++;
        }
        semana=semanas;
    }

    public void mostrarCalendarioMensual(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe,VBox[][] calendarioVBoxMensual){
        //Se pone invisible el semanal y visible el mensual
        contenedorSemanal.setVisible(false);
        calendarioMensual.setVisible(true);
        contenedorDiario.setVisible(false);
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
                if (!calendarioMensual.getChildren().contains(casillaActual)) calendarioMensual.add(casillaActual, j, i);

                casillaActual.getChildren().clear();

                String colorFondo="transparent";
                if(j==6||j==5) colorFondo = prefs.getBoolean("modo_oscuro",false) ? "#333333" : "#e4e4e4";
                casillaActual.setStyle("-fx-background-color: "+colorFondo+";");
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
                                jefe.tratarEventoClick(event,LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonth(),diaClicado), null);
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
                                jefe.tratarEventoClick(event,LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonth(),diaClicado),null);
                            });
                            //Se mira si coincide con el dia de hoy
                            if(numMes==fechaHoy.getDayOfMonth()&&fechaSeleccionada.getMonth()==fechaHoy.getMonth() && fechaSeleccionada.getYear()==fechaHoy.getYear())  label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
                            numMes++;
                        }
                    }
                }
            }
        }
    }

    public void mostrarCalendarioSemanal(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe,Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia){
        //En caso de la semana se hace invisible el mensual y visible el semanal
        calendarioMensual.setVisible(false);
        contenedorSemanal.setVisible(true);
        contenedorDiario.setVisible(false);
        contenedorSemanal.getChildren().clear();

        //Se coge el dia de la semana de la fecha seleccionada, y el lunes de la semana
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);
        LocalDate fechaHoy=LocalDate.now();

        HBox cajaNombresSemana = new HBox();
        //Es para el hueco de las horas
        Pane huecoIzquierda = new Pane();
        huecoIzquierda.setPrefWidth(60);
        huecoIzquierda.setMinWidth(60);
        cajaNombresSemana.getChildren().add(huecoIzquierda);

        Label[] cabecerasDia = new Label[7]; //Se guardan para alinearlas luego
        contenedorSemanal.getChildren().add(cajaNombresSemana);

        for(int i=0;i<7;i++) {
            Label label=new Label();
            int diaQueTocaDibujar = lunesDeEstaSemana.plusDays(i).getDayOfMonth();
            label.setText(semana[i]+"\n"+diaQueTocaDibujar);
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);

            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinHeight(80);
            HBox.setHgrow(label, Priority.ALWAYS);

            String colorFondo="transparent";
            if(i==5 || i==6) colorFondo = prefs.getBoolean("modo_oscuro",false) ? "#333333" : "#e4e4e4";
            String estiloBase = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: " + colorFondo + ";";

            cajaNombresSemana.getChildren().add(label);
            cabecerasDia[i]=label;
            LocalDate fechaExactaDelDia = lunesDeEstaSemana.plusDays(i);
            label.setOnMouseClicked(event -> {
                jefe.setFechaSeleccionada(fechaExactaDelDia);
                jefe.mostrarTareas();});
            //Para qeu se marque al clicar
            if(diaQueTocaDibujar == fechaHoy.getDayOfMonth() && fechaSeleccionada.getMonth() == fechaHoy.getMonth() && fechaSeleccionada.getYear() == fechaHoy.getYear()) {// Si es hoy: Letra grande + Borde rojo
                label.setStyle(estiloBase+"-fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6;");
            } else label.setStyle(estiloBase);// Si no es hoy: Solo letra grande
        }

        HBox filaTodoElDia = new HBox();
        filaTodoElDia.setMinHeight(30); // Altura mínima si no hay tareas
        Pane huecoTodoElDia = new Pane();
        huecoTodoElDia.setPrefWidth(60);
        huecoTodoElDia.setMinWidth(60);
        filaTodoElDia.getChildren().add(huecoTodoElDia);

        for(int i = 0; i < 7; i++){
            VBox vBoxTareasTodoDia = new VBox();
            vBoxTareasTodoDia.setSpacing(2);

            vBoxTareasTodoDia.setMinWidth(0);
            vBoxTareasTodoDia.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(vBoxTareasTodoDia, Priority.ALWAYS); // Hacemos que se estiren a lo ancho
            vBoxTareasTodoDia.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 0 1 1 0;"); // Borde abajo y a la derecha

            panelesTareasTodoDia[i] = vBoxTareasTodoDia;
            filaTodoElDia.getChildren().add(vBoxTareasTodoDia);
        }

        contenedorSemanal.getChildren().add(filaTodoElDia);

        ScrollPane scrollReal = new ScrollPane();
        scrollReal.setFitToWidth(true); // Para que no haya scroll horizontal feo
        scrollReal.setStyle("-fx-background-color: transparent;");

        HBox cajaDeTareas=new HBox();
        cajaDeTareas.setPrefHeight(24*60);

        Pane panelHoras =new Pane();
        panelHoras.setPrefHeight(24*60);
        panelHoras.setPrefWidth(60);
        panelHoras.setMinWidth(60);
        for(int i=0;i<24;i++){
            int posY=i*60;
            Text textoHora=new Text(String.format("%02d:00",i));
            textoHora.setX(5);
            textoHora.setY(posY+15);
            textoHora.setStyle("-fx-fill: gray;");
            panelHoras.getChildren().add(textoHora);
        }
        cajaDeTareas.getChildren().add(panelHoras);
        //Paneles por cada dia
        for(int i=0;i<7;i++){
            LocalDate fechaExactaDelDia = lunesDeEstaSemana.plusDays(i);
            Pane panelDia=new Pane();
            panelDia.setPrefHeight(24*60);

            panelDia.setMinWidth(0);
            panelDia.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(panelDia,Priority.ALWAYS);
            panelDia.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;"); // Línea separadora

            for(int j=0;j<24;j++){
                Line linea =new Line();
                linea.setStartX(0);
                linea.setStartY(j*60);
                linea.setEndY(j*60);
                linea.endXProperty().bind(panelDia.widthProperty());

                linea.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-width: 2px; -fx-opacity: 0.5;");
                linea.setMouseTransparent(true);

                panelDia.getChildren().add(linea);
            }

            panelDia.setOnMouseClicked(event -> {jefe.tratarEventoClick(event, fechaExactaDelDia, LocalTime.of((int) (event.getY()/60),(int) (event.getY() % 60)));});
            panelesDiasSemanales[i]=panelDia;
            cajaDeTareas.getChildren().add(panelDia);
        }
        scrollReal.setContent(cajaDeTareas);
        contenedorSemanal.getChildren().add(scrollReal);

        for(int i=0;i<7;i++){
            // Obligamos a la cabecera a medir exactamente igual que su columna de horas
            cabecerasDia[i].minWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
            cabecerasDia[i].maxWidthProperty().bind(panelesDiasSemanales[i].widthProperty());

            // Obligamos a la caja del día entero a medir exactamente igual que su columna de horas
            panelesTareasTodoDia[i].minWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
            panelesTareasTodoDia[i].maxWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
        }
    }

    public Pane mostrarCalendarioDiario(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe,VBox panelTareasTodoDiaDiario){

        contenedorSemanal.setVisible(false);
        calendarioMensual.setVisible(false);
        contenedorDiario.setVisible(true);
        contenedorDiario.getChildren().clear();

        String fechaFormateada = fechaSeleccionada.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + ", " + fechaSeleccionada.getDayOfMonth() + " de " + fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        Label tituloFecha = new Label(fechaFormateada);
        tituloFecha.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px;");
        tituloFecha.setAlignment(Pos.CENTER);
        tituloFecha.setMaxWidth(Double.MAX_VALUE);
        contenedorDiario.getChildren().add(tituloFecha);

        VBox vBoxTodoElDia = new VBox();
        vBoxTodoElDia.setSpacing(5);
        vBoxTodoElDia.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");
        contenedorDiario.getChildren().add(panelTareasTodoDiaDiario);

        ScrollPane scrollReal = new ScrollPane();
        scrollReal.setFitToWidth(true); // Para que no haya scroll horizontal feo
        scrollReal.setStyle("-fx-background-color: transparent;");

        HBox cajaDeTareas=new HBox();
        cajaDeTareas.setPrefHeight(24*60);

        Pane panelHoras =new Pane();
        panelHoras.setPrefHeight(24*60);
        panelHoras.setPrefWidth(60);
        panelHoras.setMinWidth(60);
        for(int i=0;i<24;i++){
            int posY=i*60;
            Text textoHora=new Text(String.format("%02d:00",i));
            textoHora.setX(5);
            textoHora.setY(posY+15);
            textoHora.setStyle("-fx-fill: gray;");
            panelHoras.getChildren().add(textoHora);
        }
        cajaDeTareas.getChildren().add(panelHoras);
        //Paneles por cada dia
        Pane panelDia=new Pane();
        panelDia.setPrefHeight(24*60);

        panelDia.setMinWidth(0);
        panelDia.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(panelDia,Priority.ALWAYS);
        panelDia.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;"); // Línea separadora

        for(int j=0;j<24;j++){
            Line linea =new Line();
            linea.setStartX(0);
            linea.setStartY(j*60);
            linea.setEndY(j*60);
            linea.endXProperty().bind(panelDia.widthProperty());

            linea.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-width: 2px; -fx-opacity: 0.5;");
            linea.setMouseTransparent(true);

            panelDia.getChildren().add(linea);
        }
        panelDia.setOnMouseClicked(event -> {jefe.tratarEventoClick(event, fechaSeleccionada,LocalTime.of((int) (event.getY()/60),(int) (event.getY() % 60)));});
        cajaDeTareas.getChildren().add(panelDia);
        scrollReal.setContent(cajaDeTareas);
        contenedorDiario.getChildren().add(scrollReal);

        return panelDia;
    }
}