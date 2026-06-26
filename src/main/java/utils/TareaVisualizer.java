package utils;

import controller.MenuPrincipalController;
import model.*;
import view.View;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static javafx.scene.paint.Color.web;

public class TareaVisualizer {

    //Patron singletone
    private static final TareaVisualizer tareaVisualizer=new TareaVisualizer();
    private TareaVisualizer (){}

    public static TareaVisualizer getTareaVisualizer() {
        return tareaVisualizer;
    }

    private GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    private static final String TEXT_SIN_ETIQUETA ="Sin Etiqueta";
    private static final String TEXTFX_BACKGROUNDCOLOR ="-fx-background-color: ";
    private static final String TEXT_TAREA_SIN_ETIQUETA ="tarea-sin-etiqueta";


    public void mostrarEtiquetasClasificaciones(GridPane vBoxEtiquetas, MenuPrincipalController jefe){
        //Para mostrar las etiquetas
        vBoxEtiquetas.getChildren().clear();
        List<Etiqueta> listaEtiquetas=gestorTareas.getListaEtiquetas().stream().filter(etiqueta -> etiqueta.nombreEtiqueta() != null).filter(etiqueta -> !etiqueta.nombreEtiqueta().trim().equalsIgnoreCase(gestorTareas.getEtiquetaNeutra().nombreEtiqueta().trim())).toList();
        int i=0;
        for(Etiqueta etiqueta : listaEtiquetas){
            //Se muestran todas menos la etiqueta neutra o sin etiqueta
            if(!Objects.equals(etiqueta.nombreEtiqueta(), TEXT_SIN_ETIQUETA)){
                vBoxEtiquetas.add(new Label(etiqueta.nombreEtiqueta()),1,i);
                String colorHex = etiqueta.codColor();
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
                button.setOnMouseClicked(event -> jefe.borrarEtiqueta(etiqueta));
                vBoxEtiquetas.add(button,2,i);
                i++;
            }
        }
    }

    //En el calentadio mensual para mostrarlas
    public void mostrarEtiquetasMensuales(Etiqueta etiqueta, String buscadorTareas, LocalDate fechaSeleccionada,VBox[][] calendarioVBoxMensual,MenuPrincipalController jefe) {
        //Obtenemos todas las tareas
        List<Tarea> listaTareas = gestorTareas.getTodasTareas();
        if (etiqueta != null && !etiqueta.nombreEtiqueta().equals(TEXT_SIN_ETIQUETA))
            listaTareas = listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null && tarea.getEtiqueta().equals(etiqueta)).toList();

        String textoBusqueda = buscadorTareas != null ? buscadorTareas.toLowerCase() : "";
        if (!textoBusqueda.isEmpty())
            listaTareas = listaTareas.stream().filter(tarea -> tarea.getNombreTarea().toLowerCase().contains(textoBusqueda)).toList();

        //Obtenemos el primer di del mes
        int primerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        //Es para evitar que cada dia supere la cantidad maxima de etiquetas
        int[] maxEtiquetasCalendario = new int[32];

        LocalDate inicioMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        //Un bucle que va poniendo todas las tareas
        for (Tarea tarea : listaTareas) {
            //Obtenemos la tareas y sus posiciones de donde va
            String titulo = tarea.getNombreTarea();
            LocalDate inicioTarea = tarea.getFechaInicio();
            LocalDate finTarea = tarea.getFechaFin();

            //Si ocurre este mes
            if (inicioTarea != null && finTarea != null && !inicioTarea.isAfter(finMes) && !finTarea.isBefore(inicioMes)) {
                LocalDate pintarDesde = inicioTarea.isBefore(inicioMes) ? inicioMes : inicioTarea;
                LocalDate pintarHasta = finTarea.isAfter(finMes) ? finMes : finTarea;

                for (LocalDate fecha = pintarDesde; !fecha.isAfter(pintarHasta); fecha = fecha.plusDays(1)) {
                    //Se calcula en que cuadrado de la mtriz le toca
                    int pos = (fecha.getDayOfMonth() - 2) + primerDiaMes;
                    int columna = pos % 7;
                    int fila = (pos / 7) + 1;
                    //En caso de que ese dia tenga menos de dos etiquetas se pone una mas
                    if (maxEtiquetasCalendario[fecha.getDayOfMonth()] <= 1) {
                        //se comprueba que esta en el mismo mes y año que aparece en pantalla
                        if (fecha.getMonth().equals(fechaSeleccionada.getMonth()) && fecha.getYear() == fechaSeleccionada.getYear()) {
                            Label label = new Label((titulo));
                            //Dependiendo del estado se ve mas o menos la etiqueta
                            if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) label.setOpacity(0.2);
                            else label.setOpacity(1);
                            Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
                            infoFlotante.setShowDelay(Duration.millis(100));
                            label.setTooltip(infoFlotante);
                            //Se ven distintos wi tiene etiqueta o no
                            if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) {
                                String colorHex = tarea.getEtiqueta().codColor();
                                label.setStyle(
                                        TEXTFX_BACKGROUNDCOLOR + colorHex + ";" +
                                                "-fx-border-color: derive(" + colorHex + ", -60%);" +
                                                "-fx-border-width: 2px;" +
                                                "-fx-border-radius: 5px;" +                           // Esquinas del borde redondeadas
                                                "-fx-background-radius: 5px;" +                        // Esquinas del fondo idénticas para que encajen
                                                "-fx-text-fill: white;" +                             // Texto blanco para que contraste con el fondo relleno
                                                "-fx-font-weight: bold;"                         // Texto en negrita para que se lea mejor
                                );
                            } else label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);

                            label.setContextMenu(crearMenuContextual(tarea, jefe));
                            label.setOnDragDetected(event -> {
                                javafx.scene.input.Dragboard db = label.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                                content.putString(tarea.getIdTarea()); // Metemos el ID de la tarea en la mochila
                                db.setContent(content);
                                event.consume();
                            });

                            calendarioVBoxMensual[columna][fila].getChildren().add(label);
                        }
                        //Se mira si llega a las tres etiquetas, si es asi se ponen tres puntos indicando qeu hay mas
                    } else if (maxEtiquetasCalendario[fecha.getDayOfMonth()] == 2 && fecha.getMonth().equals(fechaSeleccionada.getMonth()) && fecha.getYear() == fechaSeleccionada.getYear()) {
                        calendarioVBoxMensual[columna][fila].getChildren().add(new Label("..."));
                    }
                    maxEtiquetasCalendario[fecha.getDayOfMonth()]++;
                }
            }
        }
    }

    //PAra mostra las tareas en el panel diario
    public void mostarEtiquetasDiarias(Pane panelDiaro, VBox panelTareasTodoDiaDiario, String textoBusqueda,Etiqueta etiqueta,LocalDate fechaSeleccionada,MenuPrincipalController jefe) {
        //Se limpia el panel
        if (panelDiaro != null) panelDiaro.getChildren().removeIf(nodo -> nodo instanceof Label);
        if (panelTareasTodoDiaDiario != null) panelTareasTodoDiaDiario.getChildren().clear();

        List<Tarea> listaTareas = gestorTareas.getTodasTareas();

        //Se mira qeu que condiciones hay (etiquetas o en la busqueta)
        if (etiqueta != null && !etiqueta.nombreEtiqueta().equals(TEXT_SIN_ETIQUETA))
            listaTareas = listaTareas.stream().filter(t -> t.getEtiqueta() != null && t.getEtiqueta().equals(etiqueta)).toList();
        if (textoBusqueda != null && !textoBusqueda.isEmpty())
            listaTareas = listaTareas.stream().filter(t -> t.getNombreTarea().toLowerCase().contains(textoBusqueda.toLowerCase())).toList();
        listaTareas = listaTareas.stream()
                .sorted(Comparator.comparing((Tarea t) -> t.getHoraInicio() == null) //  el día primero
                        .thenComparing(Tarea::getHoraInicio, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Tarea::getNombreTarea))
                .toList();
        //se recorren las tareas a mostrar
        for (Tarea tarea : listaTareas) {
            //Se comprueban que son hoy
            if (tarea.getFechaInicio() != null && tarea.getFechaFin() != null &&
                    !fechaSeleccionada.isBefore(tarea.getFechaInicio()) && !fechaSeleccionada.isAfter(tarea.getFechaFin())) {

                if (tarea.getHoraInicio() != null&& tarea.getFechaInicio().isEqual(tarea.getFechaFin())) {
                    //Se calcula su tamaño
                    int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
                    int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;

                    Label label = new Label(tarea.getNombreTarea());
                    label.setWrapText(true);
                    label.setPrefWidth(120); // Ancho fijo para evitar apachurramiento

                    // Lógica de búsqueda de carril libre, para qeu no se superponan las tareas
                    double offsetX = 0;
                    boolean ocupado = true;
                    while (ocupado) {
                        ocupado = false;
                        java.util.Objects.requireNonNull(panelDiaro, "El panel diario no puede ser nulo para calcular colisiones");                        for (javafx.scene.Node node : panelDiaro.getChildren()) {
                            if (node instanceof Label existente) {
                                boolean choqueY = minutosInicio < (existente.getLayoutY() + existente.getPrefHeight()) && (minutosInicio + duracion) > existente.getLayoutY();
                                boolean choqueX = Math.abs(existente.getLayoutX() - offsetX) < 10;
                                if (choqueY && choqueX) {
                                    offsetX += 125; // Nos movemos a la derecha si choca
                                    ocupado = true;
                                    break;
                                }
                            }
                        }
                    }
                    //se aplica ese desplazamiento
                    label.setLayoutY(minutosInicio);
                    label.setLayoutX(offsetX);
                    label.setPrefHeight(Math.max(duracion, 20));

                    // Estilos y eventos
                    if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) label.setOpacity(0.2);
                    if (tarea.getEtiqueta() != null && !tarea.getEtiqueta().nombreEtiqueta().equals(TEXT_SIN_ETIQUETA))
                        label.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold;");
                    else label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);

                    label.setContextMenu(crearMenuContextual(tarea,jefe));
                    //Si se clica el boton principal se muestra la tarea
                    label.setOnMouseClicked(e -> { if(e.getButton() == MouseButton.PRIMARY) {
                        try {
                            View.showTareaVentana(tarea);
                            jefe.mostrarCalendario();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    });
                    panelDiaro.getChildren().add(label);
                }
                //si la tarea es del dia entero
                else {
                    //Se pone el label en otro sitio
                    Label labelTodoDia = new Label(tarea.getNombreTarea());
                    labelTodoDia.setMaxWidth(Double.MAX_VALUE);
                    labelTodoDia.getStyleClass().add("tarea-todo-dia");
                    labelTodoDia.setTooltip(new Tooltip(tarea.mostrarTarea()));
                    labelTodoDia.setContextMenu(crearMenuContextual(tarea,jefe));
                    labelTodoDia.setOnMouseClicked(e -> { if(e.getButton() == MouseButton.PRIMARY) {
                        try {
                            View.showTareaVentana(tarea);
                            jefe.mostrarCalendario();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    });
                    java.util.Objects.requireNonNull(panelTareasTodoDiaDiario, "El panel de tareas de todo el día diario no puede ser nulo");

                    panelTareasTodoDiaDiario.getChildren().add(labelTodoDia);
                }
            }
        }
    }

    //Para mostrar las tareas de esa semana
    public void mostrarEtiquetasSemanales(Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia, String textoBusqueda, Etiqueta etiqueta, LocalDate fechaSeleccionada, MenuPrincipalController jefe){
        //se limpia las tareas qeu puedieran quedar
        for (Pane panel : panelesDiasSemanales) {
            if (panel != null) panel.getChildren().removeIf(nodo -> nodo instanceof Label);//Solo se borran las etiqeuta de tareas, no el resto (lineas)
        }
        for (VBox panelTodoDia : panelesTareasTodoDia) {
            if (panelTodoDia != null) panelTodoDia.getChildren().clear();
        }
        //Se cogen todas las tareas y se filtra npor condiciones (etiquetas o busqueda)
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        if(etiqueta != null && !etiqueta.nombreEtiqueta().equals(TEXT_SIN_ETIQUETA)) listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();

        String busquedaMinuscula = textoBusqueda != null ? textoBusqueda.toLowerCase().trim() : "";
        if(!busquedaMinuscula.isEmpty()) {
            listaTareas = listaTareas.stream()
                    .filter(t -> t.getNombreTarea().toLowerCase().contains(busquedaMinuscula))
                    .toList();
        }
        //Que dia empieza la seman
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1L);

        //Se recorren todos los dias
        for(Tarea tarea:listaTareas){
            for (int i = 0; i < 7; i++) {
                LocalDate diaEvaluado = lunesDeEstaSemana.plusDays(i);
                //Si ocurre ese dia
                if (tarea.getFechaInicio() != null && tarea.getFechaFin() != null &&
                        !diaEvaluado.isBefore(tarea.getFechaInicio()) && !diaEvaluado.isAfter(tarea.getFechaFin())) {

                    //Si la tera tiene horas limitadas
                    if (tarea.getHoraInicio() != null && tarea.getFechaInicio().equals(tarea.getFechaFin())) {
                        Pane panelDestino = panelesDiasSemanales[i];
                        //Se calcula su tamaño
                        int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
                        int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;

                        //se calcula el offseta a la derecha para que no se superponan
                        Label label = new Label(tarea.getNombreTarea());
                        double offsetX = 0;
                        for (javafx.scene.Node node : panelDestino.getChildren()) {
                            if (node instanceof Label existente) {
                                double existenteY = existente.getLayoutY();
                                double existenteAlto = existente.getPrefHeight();
                                if (minutosInicio < (existenteY + existenteAlto) && (minutosInicio + duracion) > existenteY) {
                                    offsetX = 60;
                                    break;
                                }
                            }
                        }
                        //se coloca en la posicion calculada
                        label.setLayoutY(minutosInicio);
                        label.setPrefHeight(Math.max(duracion, 20));
                        label.setPrefWidth(offsetX == 0 ? 120 : 60);
                        label.setLayoutX(offsetX);
                        label.setWrapText(true);

                        //Para la informacion flotante
                        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
                        infoFlotante.setShowDelay(Duration.millis(100));
                        label.setTooltip(infoFlotante);

                        label.setOpacity(tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO ? 0.2 : 1);

                        //Se aplica estilos a las etiquetas
                        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) label.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                        else label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);

                        label.setContextMenu(crearMenuContextual(tarea, jefe));
                        label.setOnMouseClicked(event -> {
                            try {
                                View.showTareaVentana(tarea);
                                jefe.mostrarCalendario();
                            } catch (Exception ignored) {
                            }
                        });
                        panelDestino.getChildren().add(label);

                    } else {
                        //Para las tareas que son del dia entero, sin hora fin
                        Label labelTodoDia = new Label(tarea.getNombreTarea());
                        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
                        infoFlotante.setShowDelay(Duration.millis(100));
                        labelTodoDia.setTooltip(infoFlotante);
                        labelTodoDia.setContextMenu(crearMenuContextual(tarea, jefe));

                        labelTodoDia.setMaxWidth(Double.MAX_VALUE);

                        //se aplica nestilos
                        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) labelTodoDia.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2;");
                        else labelTodoDia.getStyleClass().add("tarea-todo-dia");

                        // Usamos la 'i' del bucle para ponerlo en la columna exacta que estamos evaluando
                        panelesTareasTodoDia[i].getChildren().add(labelTodoDia);

                        labelTodoDia.setOnMouseClicked(event -> {
                            try {
                                View.showTareaVentana(tarea);
                                jefe.mostrarCalendario();
                            } catch (Exception ignored) {}
                        });
                    }
                }
            }
        }
    }

    //Para crear los textos qeu aparecen al hacer click derecho sobre la tarea, un menu rapido
    private ContextMenu crearMenuContextual(Tarea tarea,MenuPrincipalController jefe) {
        ContextMenu menu = new ContextMenu();
        MenuItem itemEditar = new MenuItem("✏️ Editar");
        MenuItem itemCompletar = new MenuItem("✅ Completar");
        MenuItem itemBorrar = new MenuItem("🗑️ Borrar");

        itemEditar.setOnAction(e -> {try { View.showTareaVentana(tarea);
            jefe.mostrarCalendario();
        } catch (Exception ignored) {}});
        itemCompletar.setOnAction(e -> {
            tarea.setEstadoTarea(EstadoTarea.COMPLETADA);
            ConexionBD.getConexionBD().guardarTarea(tarea);
            jefe.mostrarCalendario();
        });
        itemBorrar.setOnAction(e -> {
            gestorTareas.eliminarTarea(tarea);
            jefe.mostrarCalendario();
            jefe.mostrarTareas();
        });
        menu.getItems().addAll(itemEditar, itemCompletar, itemBorrar);
        return menu;
    }
}