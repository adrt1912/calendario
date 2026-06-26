package Utils;

import Controller.MenuPrincipalController;
import Model.GestorTareas;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CalendarioRender {

    private final Preferences prefs; //Para guardar de forma global si esta en modo oscuro o no

    //Patrón singletone
    private static CalendarioRender calendarioRender;

    public static CalendarioRender getCalendarioRender() {
        if (calendarioRender == null) calendarioRender = new CalendarioRender();
        return calendarioRender;
    }

    private CalendarioRender(){
        calendarioRender=this;
        prefs = Preferences.userNodeForPackage(GestorTareas.class);
    }

    private final GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Para los nombres de la semana
    private String[] semana;

    //Inicia todos los cuadrados del mes
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

    //da valor al array de string con los nombres de la semana segun el idioma
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

    public void mostrarCalendarioMensual(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe, VBox[][] calendarioVBoxMensual){
        //Se pone invisible el semanal y el diario, y visible el mensual
        contenedorSemanal.setVisible(false);
        calendarioMensual.setVisible(true);
        contenedorDiario.setVisible(false);

        //Obtenemos cuantos dias tiene el mes y el primer dia
        int numDiasMes = fechaSeleccionada.lengthOfMonth();
        int fechaPrimerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();

        LocalDate fechaHoy = LocalDate.now();
        int numMes = 1;

        //Recorremos casialla a casilla
        for(int i=0; i<calendarioMensual.getRowCount(); i++){
            for (int j=0; j< calendarioMensual.getColumnCount(); j++){
                VBox casillaActual = calendarioVBoxMensual[j][i];
                if (!calendarioMensual.getChildren().contains(casillaActual)) calendarioMensual.add(casillaActual, j, i);

                casillaActual.getChildren().clear();

                // Limpiamos eventos residuales de meses anteriores por si esta casilla ahora está vacía
                casillaActual.setOnMouseClicked(null);
                casillaActual.setOnDragOver(null);
                casillaActual.setOnDragEntered(null);
                casillaActual.setOnDragExited(null);
                casillaActual.setOnDragDropped(null);

                boolean esOscuro = prefs.getBoolean("modo_oscuro", false);
                String colorBorde = esOscuro ? "#555555" : "#666666";
                String colorFondo="transparent";

                if(j==6||j==5) colorFondo = prefs.getBoolean("modo_oscuro",false) ? "#333333" : "#e4e4e4";
                casillaActual.setStyle("-fx-background-color: " + colorFondo + "; " +
                        "-fx-border-color: " + colorBorde + "; " +
                        "-fx-border-width: 0 1 1 0; " +
                        "-fx-background-insets: 0 1 1 0;");
                // Fila 0: Nombres de los días de la semana
                if(i == 0)
                { Label label=new Label(semana[j]);
                    label.setFont(Font.font(16));
                    casillaActual.getChildren().add(label);

                }
                //El resto de filas van ya con numero
                else {
                    // Calculamos si la casilla actual corresponde a un día real del mes
                    boolean esDiaValido = false;
                    if(i == 1 && j >= fechaPrimerDiaMes - 1) esDiaValido = true; // Primera semana
                    else if (i > 1 && numMes <= numDiasMes) esDiaValido = true;  // Resto del mes

                    if (esDiaValido) {
                        //Creamos la etiqueta donde ira el numero
                        Label label = new Label(numMes+"");
                        casillaActual.getChildren().add(label);
                        label.setFont(Font.font(16));

                        int diaClicado = numMes;
                        LocalDate fechaDeEstaCasilla = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), diaClicado);

                        //Ponemos el evento de al clicar en el dia se trate como se deba
                        casillaActual.setOnMouseClicked(event -> jefe.tratarEventoClick(event, fechaDeEstaCasilla, null));

                        // Listener para mover las etiquetas

                        casillaActual.setOnDragOver(event -> {
                            if (event.getGestureSource() != casillaActual && event.getDragboard().hasString()) event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                            event.consume();
                        });
                        casillaActual.setOnDragEntered(event -> {
                            if (event.getGestureSource() != casillaActual && event.getDragboard().hasString()) casillaActual.setStyle("-fx-background-color: #d0e8f2; -fx-border-color: #0078D7; -fx-border-width: 2px;");
                        });

                        int finalJ = j;
                        casillaActual.setOnDragExited(event -> {
                            String colorOriginal = (finalJ == 5 || finalJ == 6) ? (prefs.getBoolean("modo_oscuro", false) ? "#333333" : "#e4e4e4") : "transparent";
                            casillaActual.setStyle("-fx-background-color: " + colorOriginal + ";");
                        });

                        casillaActual.setOnDragDropped(event -> {
                            javafx.scene.input.Dragboard db = event.getDragboard();
                            boolean exito = false;
                            if (db.hasString()) {
                                String idTareaArrastrada = db.getString();
                                jefe.moverTareaA(idTareaArrastrada, fechaDeEstaCasilla);
                                exito = true;
                            }
                            event.setDropCompleted(exito);
                            event.consume();
                        });

                        // Marcar el día de hoy
                        if(numMes == fechaHoy.getDayOfMonth() && fechaSeleccionada.getMonth() == fechaHoy.getMonth() && fechaSeleccionada.getYear() == fechaHoy.getYear()) {
                            label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
                        }
                        numMes++;
                    }
                }
            }
        }
    }

    //Para mostrar el calendario semanal
    public void mostrarCalendarioSemanal(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe,Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia){
        //En caso de la semana se hace invisible el mensual y el diarios, y visible el semanal
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

        //Se recorre los sdias
        for(int i=0;i<7;i++) {
            Label label=new Label();
            int diaQueTocaDibujar = lunesDeEstaSemana.plusDays(i).getDayOfMonth();
            //Se pone el texto con el nombre del dia de la semana
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
            //Se pone el listener al clicar
            label.setOnMouseClicked(event -> {
                jefe.setFechaSeleccionada(fechaExactaDelDia);
                jefe.mostrarTareas();});

            //Para q8e se marque al ser hoy
            if(diaQueTocaDibujar == fechaHoy.getDayOfMonth() && fechaSeleccionada.getMonth() == fechaHoy.getMonth() && fechaSeleccionada.getYear() == fechaHoy.getYear()) {// Si es hoy: Letra grande + Borde rojo
                label.setStyle(estiloBase+"-fx-font-size: 15px; -fx-font-weight: bold; -fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6;");
            } else label.setStyle(estiloBase);// Si no es hoy: Solo letra grande
        }

        //Para las tareas del dia entero
        HBox filaTodoElDia = new HBox();
        filaTodoElDia.setMinHeight(30); // Altura mínima si no hay tareas
        Pane huecoTodoElDia = new Pane();
        huecoTodoElDia.setPrefWidth(60);
        huecoTodoElDia.setMinWidth(60);
        filaTodoElDia.getChildren().add(huecoTodoElDia);

        //Se van añadiendo en cada dia
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

        //Para las tareas de varias horas
        contenedorSemanal.getChildren().add(filaTodoElDia);

        ScrollPane scrollReal = new ScrollPane();
        scrollReal.setFitToWidth(true); // Para que no haya scroll horizontal feo
        scrollReal.setStyle("-fx-background-color: transparent;");

        //Cada hora son 60 pixeles
        HBox cajaDeTareas=new HBox();
        cajaDeTareas.setPrefHeight(24*60);

        Pane panelHoras =new Pane();
        panelHoras.setPrefHeight(24*60);
        panelHoras.setPrefWidth(60);
        panelHoras.setMinWidth(60);
        //Se recorren hasta 24, poniendo las horas
        for(int i=0;i<24;i++){
            int posY=i*60;
            Text textoHora=new Text(String.format("%02d:00",i));
            textoHora.setFont(Font.font(16));
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
                //Por cad ahora se mete una lineas para facilitar su vision
                Line linea =new Line();
                linea.setStartX(0);
                linea.setStartY(j*60);
                linea.setEndY(j*60);
                linea.endXProperty().bind(panelDia.widthProperty());

                linea.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-width: 2px; -fx-opacity: 0.5;");
                linea.setMouseTransparent(true);

                panelDia.getChildren().add(linea);
            }

            //Si se cica salte el evento
            panelDia.setOnMouseClicked(event -> jefe.tratarEventoClick(event, fechaExactaDelDia, LocalTime.of((int) (event.getY()/60),(int) (event.getY() % 60))));
            panelesDiasSemanales[i]=panelDia;
            cajaDeTareas.getChildren().add(panelDia);
        }
        scrollReal.setContent(cajaDeTareas);
        contenedorSemanal.getChildren().add(scrollReal);

        //Para ajustar los tamaños bien
        for(int i=0;i<7;i++){
            // Obligamos a la cabecera a medir exactamente igual que su columna de horas
            cabecerasDia[i].minWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
            cabecerasDia[i].maxWidthProperty().bind(panelesDiasSemanales[i].widthProperty());

            // Obligamos a la caja del día entero a medir exactamente igual que su columna de horas
            panelesTareasTodoDia[i].minWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
            panelesTareasTodoDia[i].maxWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
        }
    }

    //PAra mostrar el modo dia
    public Pane mostrarCalendarioDiario(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe,VBox panelTareasTodoDiaDiario){
        //Se ponene invisibles el semanal y mensual y el diario visible
        contenedorSemanal.setVisible(false);
        calendarioMensual.setVisible(false);
        contenedorDiario.setVisible(true);
        contenedorDiario.getChildren().clear();

        String fechaFormateada = fechaSeleccionada.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + ", " + fechaSeleccionada.getDayOfMonth() + " de " + fechaSeleccionada.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        //Se pone el tiutolo de lafecha
        Label tituloFecha = new Label(fechaFormateada);
        tituloFecha.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px;");
        tituloFecha.setAlignment(Pos.CENTER);
        tituloFecha.setMaxWidth(Double.MAX_VALUE);
        contenedorDiario.getChildren().add(tituloFecha);

        //se cra el panel para taresa que son del dia entero
        panelTareasTodoDiaDiario.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0; -fx-padding: 5px;");
        contenedorDiario.getChildren().add(panelTareasTodoDiaDiario);

        //Se crea un scrollpane para las tareas de varias horas
        ScrollPane scrollReal = new ScrollPane();
        scrollReal.setFitToWidth(true); // Para que no haya scroll horizontal feo
        scrollReal.setStyle("-fx-background-color: transparent;");

        HBox cajaDeTareas=new HBox();
        cajaDeTareas.setPrefHeight(24*60);

        Pane panelHoras =new Pane();
        panelHoras.setPrefHeight(24*60);
        panelHoras.setPrefWidth(60);
        panelHoras.setMinWidth(60);
        //Se ponen las horas a la derecha
        for(int i=0;i<24;i++){
            int posY=i*60;
            Text textoHora=new Text(String.format("%02d:00",i));
            textoHora.setFont(Font.font(16));
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

        //Se ponen las lineas de las horas
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
        //si se clica en la tarea se trata
        panelDia.setOnMouseClicked(event -> jefe.tratarEventoClick(event, fechaSeleccionada,LocalTime.of((int) (event.getY()/60),(int) (event.getY() % 60))));
        cajaDeTareas.getChildren().add(panelDia);
        scrollReal.setContent(cajaDeTareas);
        contenedorDiario.getChildren().add(scrollReal);

        return panelDia;
    }
}