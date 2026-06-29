package utils;

import controller.MenuPrincipalController;
import model.GestorTareas;
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
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CalendarioRender {

    private final Preferences prefs; //Para guardar de forma global si esta en modo oscuro o no

    //Patrón singletone
    private static CalendarioRender calendarioRender;

    private static final String TEXTO_DARK = "modo_oscuro";

    private static final String TEXT_TRANSPARENTE ="transparent";

    private static final String TEXTCOD_COLOR_1 = "#333333";

    private static final String TEXTCOD_COLOR_2 ="#e4e4e4";

    public static CalendarioRender getCalendarioRender() {
        if (calendarioRender == null) calendarioRender = new CalendarioRender();
        return calendarioRender;
    }

    private CalendarioRender(){
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

    public void mostrarCalendarioMensual(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe, VBox[][] calendarioVBoxMensual) {
        // . Sincronizamos la visibilidad del panel de vistas
        contenedorSemanal.setVisible(false);
        calendarioMensual.setVisible(true);
        contenedorDiario.setVisible(false);

        // . Preparamos las variables métricas del mes actual
        int numDiasMes = fechaSeleccionada.lengthOfMonth();
        int fechaPrimerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        LocalDate fechaHoy = LocalDate.now(ZoneId.systemDefault());
        int numMes = 1;

        // 🚀 3. Orquestamos el bucle de renderizado de celdas delegando la carga pesada
        for (int i = 0; i < calendarioMensual.getRowCount(); i++) {
            for (int j = 0; j < calendarioMensual.getColumnCount(); j++) {
                VBox casillaActual = calendarioVBoxMensual[j][i];

                if (!calendarioMensual.getChildren().contains(casillaActual)) calendarioMensual.add(casillaActual, j, i);

                limpiarYEstilarCasilla(casillaActual, j);

                if (i == 0) {
                    // Fila de cabecera: Nombres de los días
                    Label label = new Label(semana[j]);
                    label.setFont(Font.font(16));
                    casillaActual.getChildren().add(label);
                } else if ((i == 1 && j >= fechaPrimerDiaMes - 1) || (i > 1 && numMes <= numDiasMes)) {
                    // Filas del cuerpo: Días válidos del mes
                    renderizarDiaCalendario(casillaActual, numMes, fechaSeleccionada, fechaHoy, jefe, j);
                    numMes++;
                }
            }
        }
    }

    // SUBMETODO 1: Purgado de listeners antiguos y cálculo estético de bordes/fondos
    private void limpiarYEstilarCasilla(VBox casilla, int col) {
        casilla.getChildren().clear();
        casilla.setOnMouseClicked(null);
        casilla.setOnDragOver(null);
        casilla.setOnDragEntered(null);
        casilla.setOnDragExited(null);
        casilla.setOnDragDropped(null);

        boolean esOscuro = prefs.getBoolean(TEXTO_DARK, false);
        String colorBorde = esOscuro ? "#555555" : "#666666";
        String colorFondo = TEXT_TRANSPARENTE;

        if (col == 6 || col == 5) colorFondo = esOscuro ? TEXTCOD_COLOR_1 : TEXTCOD_COLOR_2;


        casilla.setStyle("-fx-background-color: " + colorFondo + "; " +
                "-fx-border-color: " + colorBorde + "; " +
                "-fx-border-width: 0 1 1 0; " +
                "-fx-background-insets: 0 1 1 0;");
    }

    //  SUBMETODO 2: Inyección de la etiqueta de texto numérica y activación de marcas de tiempo
    private void renderizarDiaCalendario(VBox casilla, int numMes, LocalDate fechaSeleccionada, LocalDate fechaHoy, MenuPrincipalController jefe, int col) {
        Label label = new Label(String.valueOf(numMes));
        label.setFont(Font.font(16));
        casilla.getChildren().add(label);

        LocalDate fechaDeEstaCasilla = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), numMes);

        configurarEventosInteractivos(casilla, fechaDeEstaCasilla, jefe, col);
        verificarYMarcarDiaHoy(label, numMes, fechaSeleccionada, fechaHoy);
    }

    //  SUBMETODO 3: Aislamiento completo de los listeners del sistema Drag & Drop de JavaFX
    private void configurarEventosInteractivos(VBox casilla, LocalDate fechaCasilla, MenuPrincipalController jefe, int col) {
        casilla.setOnMouseClicked(event -> jefe.tratarEventoClick(event, fechaCasilla, null));

        casilla.setOnDragOver(event -> {
            if (event.getGestureSource() != casilla && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
            event.consume();
        });

        casilla.setOnDragEntered(event -> {
            if (event.getGestureSource() != casilla && event.getDragboard().hasString()) {
                casilla.setStyle("-fx-background-color: #d0e8f2; -fx-border-color: #0078D7; -fx-border-width: 2px;");
            }
        });

        casilla.setOnDragExited(event -> {
            boolean esOscuro = prefs.getBoolean(TEXTO_DARK, false);
            String colorOriginal;
            if (col == 5 || col == 6) colorOriginal = esOscuro ? TEXTCOD_COLOR_1 : TEXTCOD_COLOR_2;
             else colorOriginal = TEXT_TRANSPARENTE;
            casilla.setStyle("-fx-background-color: " + colorOriginal + ";");
        });

        casilla.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean exito = false;
            if (db.hasString()) {
                jefe.moverTareaA(db.getString(), fechaCasilla);
                exito = true;
            }
            event.setDropCompleted(exito);
            event.consume();
        });
    }

    // SUBMETODO 4: Comprobación aislada para pintar el borde circular rojo de "Hoy"
    private void verificarYMarcarDiaHoy(Label label, int numMes, LocalDate fechaSeleccionada, LocalDate fechaHoy) {
        if (numMes == fechaHoy.getDayOfMonth()
                && fechaSeleccionada.getMonth().equals(fechaHoy.getMonth())
                && fechaSeleccionada.getYear() == fechaHoy.getYear()) {
            label.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6");
        }
    }


    public void mostrarCalendarioSemanal(GridPane calendarioMensual, VBox contenedorSemanal, VBox contenedorDiario, LocalDate fechaSeleccionada, MenuPrincipalController jefe, Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia) {
        // 1. Sincronizamos e inicializamos la visibilidad de los contenedores
        calendarioMensual.setVisible(false);
        contenedorSemanal.setVisible(true);
        contenedorDiario.setVisible(false);
        contenedorSemanal.getChildren().clear();

        //  2. Calculamos las métricas temporales de la semana actual
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1L);
        LocalDate fechaHoy = LocalDate.now(ZoneId.systemDefault());

        //  3. Renderizamos la fila superior con los nombres y números de los días
        Label[] cabecerasDia = crearFilaCabecerasDias(contenedorSemanal, lunesDeEstaSemana, fechaSeleccionada, fechaHoy, jefe);

        //  4. Renderizamos la sección de tareas de  el día entero
        configurarFilaTodoElDia(contenedorSemanal, panelesTareasTodoDia);

        //  5. Construimos el cuerpo del calendario (Horas + Columnas de días con su rejilla de guías)
        HBox cajaDeTareas = new HBox();
        configurarPanelHorasLateral(cajaDeTareas);
        configurarColumnasDiasConRejilla(cajaDeTareas, panelesDiasSemanales);

        ScrollPane scrollReal = new ScrollPane(cajaDeTareas);
        scrollReal.setFitToWidth(true);
        contenedorSemanal.getChildren().add(scrollReal);

        //  6. Vinculamos dinámicamente las propiedades de ancho para una responsividad perfecta
        vincularPropiedadesAnchoSemanal(cabecerasDia, panelesDiasSemanales);
    }

    //  SUBMETODO 1: Renderiza y estila la barra de cabeceras de días de la semana
    private Label[] crearFilaCabecerasDias(VBox contenedorSemanal, LocalDate lunesDeEstaSemana, LocalDate fechaSeleccionada, LocalDate fechaHoy, MenuPrincipalController jefe) {
        HBox cajaNombresSemana = new HBox();
        Pane huecoIzquierda = new Pane();
        huecoIzquierda.setPrefWidth(60);
        huecoIzquierda.setMinWidth(60);
        cajaNombresSemana.getChildren().add(huecoIzquierda);

        Label[] cabecerasDia = new Label[7];
        contenedorSemanal.getChildren().add(cajaNombresSemana);

        for (int i = 0; i < 7; i++) {
            LocalDate fechaExactaDelDia = lunesDeEstaSemana.plusDays(i);
            int diaQueTocaDibujar = fechaExactaDelDia.getDayOfMonth();

            Label label = new Label(semana[i] + "\n" + diaQueTocaDibujar);
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinHeight(80);
            HBox.setHgrow(label, Priority.ALWAYS);

            label.setOnMouseClicked(event -> {
                jefe.setFechaSeleccionada(fechaExactaDelDia);
                jefe.mostrarTareas();
            });

            // Aplicamos el estilo de fondo y el borde rojo si coincide con el día actual
            inyectarEstiloCabecera(label, i, diaQueTocaDibujar, fechaSeleccionada, fechaHoy);

            cajaNombresSemana.getChildren().add(label);
            cabecerasDia[i] = label;
        }
        return cabecerasDia;
    }

    //  SUBMETODO 2: Construye los contenedores verticales para almacenar las tareas del día entero
    private void configurarFilaTodoElDia(VBox contenedorSemanal, VBox[] panelesTareasTodoDia) {
        HBox filaTodoElDia = new HBox();
        filaTodoElDia.setMinHeight(30);
        Pane huecoTodoElDia = new Pane();
        huecoTodoElDia.setPrefWidth(60);
        huecoTodoElDia.setMinWidth(60);
        filaTodoElDia.getChildren().add(huecoTodoElDia);

        for (int i = 0; i < 7; i++) {
            VBox vBoxTareasTodoDia = new VBox();
            vBoxTareasTodoDia.setSpacing(2);
            vBoxTareasTodoDia.setMinWidth(0);
            vBoxTareasTodoDia.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(vBoxTareasTodoDia, Priority.ALWAYS);
            vBoxTareasTodoDia.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 0;");

            filaTodoElDia.getChildren().add(vBoxTareasTodoDia);
            panelesTareasTodoDia[i] = vBoxTareasTodoDia;
        }
        contenedorSemanal.getChildren().add(filaTodoElDia);
    }

    //  SUBMETODO 3: Genera la barra de tiempo lateral con las etiquetas de texto de 00:00 a 23:00
    private void configurarPanelHorasLateral(HBox cajaDeTareas) {
        Pane panelHoras = new Pane();
        panelHoras.setPrefHeight(24 * 60);
        panelHoras.setPrefWidth(60);
        panelHoras.setMinWidth(60);

        for (int i = 0; i < 24; i++) {
            int posY = i * 60;
            Text textoHora = new Text(String.format("%02d:00", i));
            textoHora.setFont(Font.font(16));
            textoHora.setX(5);
            textoHora.setY(posY + 15);
            textoHora.setStyle("-fx-fill: gray;");
            panelHoras.getChildren().add(textoHora);
        }
        cajaDeTareas.getChildren().add(panelHoras);
    }

    //  SUBMETODO 4: Construye las columnas horarias por día e inyecta las líneas de cuadrícula divisorias
    private void configurarColumnasDiasConRejilla(HBox cajaDeTareas, Pane[] panelesDiasSemanales) {
        for (int i = 0; i < 7; i++) {
            Pane panelDia = new Pane();
            panelDia.setPrefHeight(24 * 60);
            panelDia.setMinWidth(0);
            panelDia.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(panelDia, Priority.ALWAYS);
            panelDia.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");

            for (int j = 0; j < 24; j++) {
                Line linea = new Line();
                linea.setStartX(0);
                linea.setStartY(j * 60);
                linea.setEndY(j * 60);
                // Vinculamos dinámicamente el fin de la línea al ancho del panel del día
                linea.endXProperty().bind(panelDia.widthProperty());
                linea.setStyle("-fx-stroke: #f0f0f0; -fx-stroke-width: 1;");
                panelDia.getChildren().add(linea);
            }

            cajaDeTareas.getChildren().add(panelDia);
            panelesDiasSemanales[i] = panelDia;
        }
    }

    //  SUBMeTODO 5: Vincula las propiedades de redimensionamiento para que las cabeceras sigan a sus columnas
    private void vincularPropiedadesAnchoSemanal(Label[] cabecerasDia, Pane[] panelesDiasSemanales) {
        for (int i = 0; i < 7; i++) {
            cabecerasDia[i].minWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
            cabecerasDia[i].maxWidthProperty().bind(panelesDiasSemanales[i].widthProperty());
        }
    }

    // : Centraliza de forma limpia el cálculo estético de colores del tema visual
    private void inyectarEstiloCabecera(Label label, int indiceDia, int diaQueToca, LocalDate fechaSeleccionada, LocalDate fechaHoy) {
        String colorFondo = TEXT_TRANSPARENTE;
        if (indiceDia == 5 || indiceDia == 6) {
            colorFondo = prefs.getBoolean(TEXTO_DARK, false) ? TEXTCOD_COLOR_1 : TEXTCOD_COLOR_2;
        }
        String estiloBase = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: " + colorFondo + ";";

        if (diaQueToca == fechaHoy.getDayOfMonth()
                && fechaSeleccionada.getMonth().equals(fechaHoy.getMonth())
                && fechaSeleccionada.getYear() == fechaHoy.getYear()) {
            label.setStyle(estiloBase + "-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 50em; -fx-padding: 2 6 2 6;");
        } else {
            label.setStyle(estiloBase);
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