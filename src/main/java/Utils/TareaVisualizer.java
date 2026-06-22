package Utils;

import Controller.MenuPrincipalController;
import Model.EstadoTarea;
import Model.Etiqueta;
import Model.GestorTareas;
import Model.Tarea;
import View.view;
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

    private static TareaVisualizer tareaVisualizer=new TareaVisualizer();
    private TareaVisualizer (){}

    public static TareaVisualizer getTareaVisualizer() {
        return tareaVisualizer;
    }

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    public void mostrarEtiquetasClasificaciones(GridPane vBoxEtiquetas, MenuPrincipalController jefe){
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
                    jefe.borrarEtiqueta(etiqueta);
                });
                vBoxEtiquetas.add(button,2,i);
                i++;
            }
        }
    }

    public void mostrarEtiquetasMensuales(Etiqueta etiqueta, String buscadorTareas, LocalDate fechaSeleccionada,VBox[][] calendarioVBoxMensual,MenuPrincipalController jefe) {
        //Obtenemos todas las tareas
        List<Tarea> listaTareas = gestorTareas.getTodasTareas();
        if (etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta"))
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

            if (inicioTarea != null && finTarea != null && !inicioTarea.isAfter(finMes) && !finTarea.isBefore(inicioMes)) {
                LocalDate pintarDesde = inicioTarea.isBefore(inicioMes) ? inicioMes : inicioTarea;
                LocalDate pintarHasta = finTarea.isAfter(finMes) ? finMes : finTarea;

                for (LocalDate fecha = pintarDesde; !fecha.isAfter(pintarHasta); fecha = fecha.plusDays(1)) {
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
                                label.getStyleClass().add("tarea-sin-etiqueta");
                            }
                            label.setContextMenu(crearMenuContextual(tarea, jefe));
                            label.setOnDragDetected(event -> {
                                javafx.scene.input.Dragboard db = label.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                                content.putString(tarea.getIdTarea()); // Metemos el ID de la tarea en la mochila
                                db.setContent(content);
                                event.consume();
                            });

                            calendarioVBoxMensual[columna][fila].getChildren().add(label);
                            //Se pone en el calendario
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

    public void mostarEtiquetasDiarias(Pane panelDiaro, VBox panelTareasTodoDiaDiario, String textoBusqueda,Etiqueta etiqueta,LocalDate fechaSeleccionada,MenuPrincipalController jefe) {
        // 1. Limpieza segura
        if (panelDiaro != null) panelDiaro.getChildren().removeIf(nodo -> nodo instanceof Label);
        if (panelTareasTodoDiaDiario != null) panelTareasTodoDiaDiario.getChildren().clear();

        List<Tarea> listaTareas = gestorTareas.getTodasTareas();

        if (etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta"))
            listaTareas = listaTareas.stream().filter(t -> t.getEtiqueta() != null && t.getEtiqueta().equals(etiqueta)).toList();
        if (textoBusqueda != null && !textoBusqueda.isEmpty())
            listaTareas = listaTareas.stream().filter(t -> t.getNombreTarea().toLowerCase().contains(textoBusqueda)).toList();
        listaTareas = listaTareas.stream()
                .sorted(Comparator.comparing((Tarea t) -> t.getHoraInicio() == null) // Todo el día primero
                        .thenComparing(Tarea::getHoraInicio, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Tarea::getNombreTarea))
                .toList();
        for (Tarea tarea : listaTareas) {
            if (tarea.getFechaInicio() != null && tarea.getFechaFin() != null &&
                    !fechaSeleccionada.isBefore(tarea.getFechaInicio()) && !fechaSeleccionada.isAfter(tarea.getFechaFin())) {

                // --- A) TAREAS CON HORA (Uso del bucle while para evitar solapamiento) ---
                if (tarea.getHoraInicio() != null&& tarea.getFechaInicio().isEqual(tarea.getFechaFin())) {
                    int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
                    int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;

                    Label label = new Label(tarea.getNombreTarea());
                    label.setWrapText(true);
                    label.setPrefWidth(120); // Ancho fijo para evitar apachurramiento

                    // Lógica de búsqueda de carril libre
                    double offsetX = 0;
                    boolean ocupado = true;
                    while (ocupado) {
                        ocupado = false;
                        for (javafx.scene.Node node : panelDiaro.getChildren()) {
                            if (node instanceof Label) {
                                Label existente = (Label) node;
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

                    label.setLayoutY(minutosInicio);
                    label.setLayoutX(offsetX);
                    label.setPrefHeight(Math.max(duracion, 20));

                    // Estilos y eventos
                    if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) label.setOpacity(0.2);
                    if (tarea.getEtiqueta() != null && !tarea.getEtiqueta().getNombreEtiqueta().equals("Sin Etiqueta"))
                        label.setStyle("-fx-background-color: " + tarea.getEtiqueta().getCodColor() + "; -fx-text-fill: white; -fx-font-weight: bold;");
                    else label.getStyleClass().add("tarea-sin-etiqueta");

                    label.setContextMenu(crearMenuContextual(tarea,jefe));
                    label.setOnMouseClicked(e -> { if(e.getButton() == MouseButton.PRIMARY) {
                        try {
                            view.showTareaVentana(tarea);
                            jefe.mostrarCalendario();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    });
                    panelDiaro.getChildren().add(label);
                }

                else {
                    Label labelTodoDia = new Label(tarea.getNombreTarea());
                    labelTodoDia.setMaxWidth(Double.MAX_VALUE);
                    labelTodoDia.getStyleClass().add("tarea-todo-dia");
                    labelTodoDia.setTooltip(new Tooltip(tarea.mostrarTarea()));
                    labelTodoDia.setContextMenu(crearMenuContextual(tarea,jefe));
                    labelTodoDia.setOnMouseClicked(e -> { if(e.getButton() == MouseButton.PRIMARY) {
                        try {
                            view.showTareaVentana(tarea);
                            jefe.mostrarCalendario();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    });

                    panelTareasTodoDiaDiario.getChildren().add(labelTodoDia);
                }
            }
        }
    }

    //Para mostrar las tareas de esa semana
    public void mostrarEtiquetasSemanales(Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia, String textoBusqueda, Etiqueta etiqueta, LocalDate fechaSeleccionada, MenuPrincipalController jefe){
        //Se cogen todas las tareas
        for (Pane panel : panelesDiasSemanales) {
            if (panel != null) panel.getChildren().removeIf(nodo -> nodo instanceof Label);//Solo se borran las etiqeuta de tareas, no el resto (lineas)
        }
        for (VBox panelTodoDia : panelesTareasTodoDia) {
            if (panelTodoDia != null) panelTodoDia.getChildren().clear();
        }

        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        if(etiqueta != null && !etiqueta.getNombreEtiqueta().equals("Sin Etiqueta")) listaTareas=listaTareas.stream().filter(tarea -> tarea.getEtiqueta() != null &&tarea.getEtiqueta().equals(etiqueta)).toList();

        if(textoBusqueda!=null && !textoBusqueda.isEmpty()) listaTareas=listaTareas.stream().filter(tarea -> tarea.getNombreTarea().toLowerCase().contains(textoBusqueda)).toList();

        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1);
        LocalDate domingoDeEstaSemana = lunesDeEstaSemana.plusDays(6);

        for(Tarea tarea:listaTareas){

            for (int i = 0; i < 7; i++) {
                LocalDate diaEvaluado = lunesDeEstaSemana.plusDays(i);
                if (tarea.getFechaInicio() != null && tarea.getFechaFin() != null &&
                        !diaEvaluado.isBefore(tarea.getFechaInicio()) && !diaEvaluado.isAfter(tarea.getFechaFin())) {

                    // CASO 1: Un solo día y con hora de inicio -> Va al panel de horas
                    if (tarea.getHoraInicio() != null && tarea.getFechaInicio().equals(tarea.getFechaFin())) {
                        Pane panelDestino = panelesDiasSemanales[i];
                        int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
                        int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;

                        Label label = new Label(tarea.getNombreTarea());
                        double offsetX = 0;
                        for (javafx.scene.Node node : panelDestino.getChildren()) {
                            if (node instanceof Label) {
                                Label existente = (Label) node;
                                double existenteY = existente.getLayoutY();
                                double existenteAlto = existente.getPrefHeight();
                                if (minutosInicio < (existenteY + existenteAlto) && (minutosInicio + duracion) > existenteY) {
                                    offsetX = 60;
                                    break;
                                }
                            }
                        }
                        label.setLayoutY(minutosInicio);
                        label.setPrefHeight(Math.max(duracion, 20));
                        label.setPrefWidth(offsetX == 0 ? 120 : 60);
                        label.setLayoutX(offsetX);
                        label.setWrapText(true);

                        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
                        infoFlotante.setShowDelay(Duration.millis(100));
                        label.setTooltip(infoFlotante);

                        label.setOpacity(tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO ? 0.2 : 1);

                        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().getNombreEtiqueta(), "Sin Etiqueta")) {
                            label.setStyle("-fx-background-color: " + tarea.getEtiqueta().getCodColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
                        } else {
                            label.getStyleClass().add("tarea-sin-etiqueta");
                        }

                        label.setContextMenu(crearMenuContextual(tarea, jefe));
                        label.setOnMouseClicked(event -> {
                            try {
                                view.showTareaVentana(tarea);
                                jefe.mostrarCalendario();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        panelDestino.getChildren().add(label);

                    } else {
                        // CASO 2: Varios días o "Todo el día" -> Va a la cabecera del día correspondiente
                        Label labelTodoDia = new Label(tarea.getNombreTarea());
                        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
                        infoFlotante.setShowDelay(Duration.millis(100));
                        labelTodoDia.setTooltip(infoFlotante);
                        labelTodoDia.setContextMenu(crearMenuContextual(tarea, jefe));

                        labelTodoDia.setMaxWidth(Double.MAX_VALUE);

                        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().getNombreEtiqueta(), "Sin Etiqueta")) {
                            labelTodoDia.setStyle("-fx-background-color: " + tarea.getEtiqueta().getCodColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2;");
                        } else {
                            labelTodoDia.getStyleClass().add("tarea-todo-dia");
                        }

                        // Usamos la 'i' del bucle para ponerlo en la columna exacta que estamos evaluando
                        panelesTareasTodoDia[i].getChildren().add(labelTodoDia);

                        labelTodoDia.setOnMouseClicked(event -> {
                            try {
                                view.showTareaVentana(tarea);
                                jefe.mostrarCalendario();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        }
    }

    private ContextMenu crearMenuContextual(Tarea tarea,MenuPrincipalController jefe) {
        ContextMenu menu = new ContextMenu();
        MenuItem itemEditar = new MenuItem("✏️ Editar");
        MenuItem itemCompletar = new MenuItem("✅ Completar");
        MenuItem itemBorrar = new MenuItem("🗑️ Borrar");

        itemEditar.setOnAction(e -> {try { view.showTareaVentana(tarea);
            jefe.mostrarCalendario();
        } catch (Exception ex) { ex.printStackTrace(); }});
        itemCompletar.setOnAction(e -> {
            tarea.setEstadoTarea(EstadoTarea.COMPLETADA);
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
