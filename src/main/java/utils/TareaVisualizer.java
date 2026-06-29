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
import java.util.logging.Logger;

import static javafx.scene.paint.Color.web;

public class TareaVisualizer {

    private final Logger logger = Logger.getLogger(getClass().getName());


    //Patron singletone
    private static final TareaVisualizer tareaVisualizer=new TareaVisualizer();
    private TareaVisualizer (){}

    public static TareaVisualizer getTareaVisualizer() {
        return tareaVisualizer;
    }

    private final GestorTareas gestorTareas=GestorTareas.getGestorTareas();

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

    public void mostrarEtiquetasMensuales(Etiqueta etiqueta, String buscadorTareas, LocalDate fechaSeleccionada, VBox[][] calendarioVBoxMensual, MenuPrincipalController jefe) {
        //  1. Filtramos el set de tareas mediante un pipeline limpio
        List<Tarea> listaTareas = filtrarTareasMensuales(buscadorTareas, etiqueta);

        //  2. Inicializamos las métricas del mes en curso
        int primerDiaMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        int[] maxEtiquetasCalendario = new int[32];

        LocalDate inicioMes = LocalDate.of(fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue(), 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        //  3. Orquestamos la distribución por fechas delegando la maquetación pesada
        for (Tarea tarea : listaTareas) {
            procesarTareaEnMes(tarea, inicioMes, finMes, primerDiaMes, maxEtiquetasCalendario, calendarioVBoxMensual, jefe);
        }
    }

    //  SUBMETODO 1: Pipeline aislado para resolver la barra de búsqueda y filtros
    private List<Tarea> filtrarTareasMensuales(String buscadorTareas, Etiqueta etiqueta) {
        List<Tarea> lista = gestorTareas.getTodasTareas();

        if (etiqueta != null && !TEXT_SIN_ETIQUETA.equals(etiqueta.nombreEtiqueta())) {
            lista = lista.stream()
                    .filter(tarea -> tarea.getEtiqueta() != null && tarea.getEtiqueta().equals(etiqueta))
                    .toList();
        }

        String textoBusqueda = buscadorTareas != null ? buscadorTareas.toLowerCase().trim() : "";
        if (!textoBusqueda.isEmpty()) {
            lista = lista.stream()
                    .filter(tarea -> tarea.getNombreTarea().toLowerCase().contains(textoBusqueda))
                    .toList();
        }
        return lista;
    }

    //  SUBMETODO 2: Validador de pertenencia mensual y bucle de fechas acotado
    private void procesarTareaEnMes(Tarea tarea, LocalDate inicioMes, LocalDate finMes, int primerDiaMes, int[] maxEtiquetas, VBox[][] calendarioMensual, MenuPrincipalController jefe) {
        LocalDate inicioTarea = tarea.getFechaInicio();
        LocalDate finTarea = tarea.getFechaFin();

        if (inicioTarea == null || finTarea == null || inicioTarea.isAfter(finMes) || finTarea.isBefore(inicioMes)) {
            return;
        }

        // Acotamos el rango de pintado para no salirnos de los márgenes del mes visualizado
        LocalDate pintarDesde = inicioTarea.isBefore(inicioMes) ? inicioMes : inicioTarea;
        LocalDate pintarHasta = finTarea.isAfter(finMes) ? finMes : finTarea;

        for (LocalDate fecha = pintarDesde; !fecha.isAfter(pintarHasta); fecha = fecha.plusDays(1)) {
            inyectarEtiquetaEnMatriz(tarea, fecha, primerDiaMes, maxEtiquetas, calendarioMensual, jefe);
        }
    }

    //  SUBMETODO 3: Controlador de cupo máximo de celdas y cálculo de coordenadas de la matriz
    private void inyectarEtiquetaEnMatriz(Tarea tarea, LocalDate fecha, int primerDiaMes, int[] maxEtiquetas, VBox[][] calendarioMensual, MenuPrincipalController jefe) {
        int dia = fecha.getDayOfMonth();
        int pos = (dia - 2) + primerDiaMes;
        int columna = pos % 7;
        int fila = (pos / 7) + 1;

        if (maxEtiquetas[dia] <= 1) {
            // Generamos e insertamos el nodo gráfico de la tarea
            Label label = crearLabelTareaMensual(tarea, jefe);
            calendarioMensual[columna][fila].getChildren().add(label);
        } else if (maxEtiquetas[dia] == 2) {
            // Inyectamos los puntos suspensivos si se sobrepasa el límite visual de la celda
            calendarioMensual[columna][fila].getChildren().add(new Label("..."));
        }

        maxEtiquetas[dia]++;
    }

    //  SUBMETODO 4: Factoría constructora de la etiqueta (Estilos, Opacidad, Tooltips y Drag and Drop)
    private Label crearLabelTareaMensual(Tarea tarea, MenuPrincipalController jefe) {
        Label label = new Label(tarea.getNombreTarea());
        label.setOpacity(tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO ? 0.2 : 1.0);

        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
        infoFlotante.setShowDelay(javafx.util.Duration.millis(100));
        label.setTooltip(infoFlotante);

        // Diseño de estilos CSS según estado de etiqueta
        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) {
            String colorHex = tarea.getEtiqueta().codColor();
            label.setStyle(
                    TEXTFX_BACKGROUNDCOLOR + colorHex + ";" +
                            "-fx-border-color: derive(" + colorHex + ", -60%);" +
                            "-fx-border-width: 2px;" +
                            "-fx-border-radius: 5px;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;"
            );
        } else {
            label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);
        }

        label.setContextMenu(crearMenuContextual(tarea, jefe));

        // Activamos el soporte nativo de Arrastrar y Soltar de JavaFX
        label.setOnDragDetected(event -> {
            javafx.scene.input.Dragboard db = label.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(tarea.getIdTarea());
            db.setContent(content);
            event.consume();
        });

        return label;
    }


    //Para mostrar las tareas de esa semana
    public void mostrarEtiquetasSemanales(Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia, String textoBusqueda, Etiqueta etiqueta, LocalDate fechaSeleccionada, MenuPrincipalController jefe) {
        //  1. Purgamos de la interfaz los residuos visuales anteriores
        limpiarPanelesSemanales(panelesDiasSemanales, panelesTareasTodoDia);

        //  2. Extraemos el set de datos filtrados correspondientes
        List<Tarea> listaTareas = filtrarTareasSemanales(textoBusqueda, etiqueta);

        //  3. Calculamos la métrica temporal de inicio de semana
        int diaDeLaSemana = fechaSeleccionada.getDayOfWeek().getValue();
        LocalDate lunesDeEstaSemana = fechaSeleccionada.minusDays(diaDeLaSemana - 1L);

        //  4. Iteramos mapeando las tareas hacia sus celdas correspondientes de forma limpia
        for (Tarea tarea : listaTareas) {
            for (int i = 0; i < 7; i++) {
                LocalDate diaEvaluado = lunesDeEstaSemana.plusDays(i);

                if (esTareaValidaParaDia(tarea, diaEvaluado)) {
                    ubicarTareaEnCalendarioSemanal(tarea, i, panelesDiasSemanales, panelesTareasTodoDia, jefe);
                }
            }
        }
    }

    //  SUBMeTODO 1: Limpieza aislada de nodos de texto e interfaces
    private void limpiarPanelesSemanales(Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia) {
        for (Pane panel : panelesDiasSemanales) {
            if (panel != null) panel.getChildren().removeIf(nodo -> nodo instanceof Label);

        }
        for (VBox panelTodoDia : panelesTareasTodoDia) {
            if (panelTodoDia != null) panelTodoDia.getChildren().clear();
        }
    }

    //  SUBMeTODO 2: Pipeline de streams aislado para la barra de búsqueda y etiquetas
    private List<Tarea> filtrarTareasSemanales(String textoBusqueda, Etiqueta etiqueta) {
        List<Tarea> lista = gestorTareas.getTodasTareas();

        if (etiqueta != null && !TEXT_SIN_ETIQUETA.equals(etiqueta.nombreEtiqueta())) {
            lista = lista.stream()
                    .filter(tarea -> tarea.getEtiqueta() != null && tarea.getEtiqueta().equals(etiqueta))
                    .toList();
        }

        String busquedaMinuscula = textoBusqueda != null ? textoBusqueda.toLowerCase().trim() : "";
        if (!busquedaMinuscula.isEmpty()) {
            lista = lista.stream()
                    .filter(t -> t.getNombreTarea().toLowerCase().contains(busquedaMinuscula))
                    .toList();
        }
        return lista;
    }

    // SUBMeTODO 3: Validador booleano de solapamiento de fechas
    private boolean esTareaValidaParaDia(Tarea tarea, LocalDate diaEvaluado) {
        return tarea.getFechaInicio() != null && tarea.getFechaFin() != null
                && !diaEvaluado.isBefore(tarea.getFechaInicio())
                && !diaEvaluado.isAfter(tarea.getFechaFin());
    }

    //  SUBMeTODO 4: Enrutador de tareas según sus restricciones horarias
    private void ubicarTareaEnCalendarioSemanal(Tarea tarea, int indiceDia, Pane[] panelesDiasSemanales, VBox[] panelesTareasTodoDia, MenuPrincipalController jefe) {
        if (tarea.getHoraInicio() != null && tarea.getFechaInicio().equals(tarea.getFechaFin())) {
            renderizarTareaConHoras(tarea, panelesDiasSemanales[indiceDia], jefe);
        } else renderizarTareaTodoElDia(tarea, panelesTareasTodoDia[indiceDia], jefe);
    }

    //  SUBMeTODO 5: Detector de colisiones horarias en el eje Y para evitar superposiciones
    private double calcularOffsetX(Pane panelDestino, int minutosInicio, int duracion) {
        for (javafx.scene.Node node : panelDestino.getChildren()) {
            if (node instanceof Label existente) {
                double existenteY = existente.getLayoutY();
                double existenteAlto = existente.getPrefHeight();

                if (minutosInicio < (existenteY + existenteAlto) && (minutosInicio + duracion) > existenteY) {
                    return 60.0; // Desplazamiento lateral para la segunda columna de tareas solapadas
                }
            }
        }
        return 0.0;
    }

    //  SUBMeTODO 6: Renderizado estético y posicionamiento de tareas por horas
    private void renderizarTareaConHoras(Tarea tarea, Pane panelDestino, MenuPrincipalController jefe) {
        int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
        int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;
        double offsetX = calcularOffsetX(panelDestino, minutosInicio, duracion);

        Label label = new Label(tarea.getNombreTarea());
        label.setLayoutY(minutosInicio);
        label.setPrefHeight(Math.max(duracion, 20));
        label.setPrefWidth(offsetX == 0 ? 120 : 60);
        label.setLayoutX(offsetX);
        label.setWrapText(true);
        label.setOpacity(tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO ? 0.2 : 1.0);

        configurarComponentesComunesLabel(label, tarea, jefe);

        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) {
            label.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        } else label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);

        panelDestino.getChildren().add(label);
    }

    //  SUBMeTODO 7: Renderizado de bloques de texto superiores de "Todo el día"
    private void renderizarTareaTodoElDia(Tarea tarea, VBox panelTodoDia, MenuPrincipalController jefe) {
        Label labelTodoDia = new Label(tarea.getNombreTarea());
        labelTodoDia.setMaxWidth(Double.MAX_VALUE);

        configurarComponentesComunesLabel(labelTodoDia, tarea, jefe);

        if (tarea.getEtiqueta() != null && !Objects.equals(tarea.getEtiqueta().nombreEtiqueta(), TEXT_SIN_ETIQUETA)) {
            labelTodoDia.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2;");
        } else {
            labelTodoDia.getStyleClass().add("tarea-todo-dia");
        }

        panelTodoDia.getChildren().add(labelTodoDia);
    }

    //  SUBMeTODO 8: Centralización de componentes interactivos (Tooltips, Menús y Eventos Click)
    private void configurarComponentesComunesLabel(Label label, Tarea tarea, MenuPrincipalController jefe) {
        Tooltip infoFlotante = new Tooltip(tarea.mostrarTarea());
        infoFlotante.setShowDelay(Duration.millis(100));
        label.setTooltip(infoFlotante);

        label.setContextMenu(crearMenuContextual(tarea, jefe));

        label.setOnMouseClicked(event -> {
            try {
                View.showTareaVentana(tarea);
                jefe.mostrarCalendario();
            } catch (Exception ignored) {
                // Mantenemos la captura de excepciones silenciosa original del diseño
            }
        });
    }

    public void mostarEtiquetasDiarias(Pane panelDiaro, VBox panelTareasTodoDiaDiario, String textoBusqueda, Etiqueta etiqueta, LocalDate fechaSeleccionada, MenuPrincipalController jefe) {
        //  1. Purgamos de la interfaz los residuos visuales anteriores
        limpiarPanelesDiarios(panelDiaro, panelTareasTodoDiaDiario);

        //  2. Extraemos el conjunto de datos filtrado y ordenado cronológicamente
        List<Tarea> listaTareas = filtrarYOrdenarTareasDiarias(textoBusqueda, etiqueta);

        //  3. Iteramos distribuyendo las tareas que ocurren hoy en sus correspondientes paneles
        for (Tarea tarea : listaTareas) {
            if (esTareaValidaParaDiaDia(tarea, fechaSeleccionada)) {
                ubicarTareaEnCalendarioDiario(tarea, panelDiaro, panelTareasTodoDiaDiario, jefe);
            }
        }
    }

    //  SUBMETODO 1: Limpieza aislada de etiquetas de tareas previas
    private void limpiarPanelesDiarios(Pane panelDiaro, VBox panelTareasTodoDiaDiario) {
        if (panelDiaro != null) panelDiaro.getChildren().removeIf(nodo -> nodo instanceof Label);

        if (panelTareasTodoDiaDiario != null) panelTareasTodoDiaDiario.getChildren().clear();
    }

    //  SUBMETODO 2: Pipeline de streams encargado del filtrado y criterios de ordenación
    private List<Tarea> filtrarYOrdenarTareasDiarias(String textoBusqueda, Etiqueta etiqueta) {
        List<Tarea> lista = gestorTareas.getTodasTareas();

        if (etiqueta != null && !TEXT_SIN_ETIQUETA.equals(etiqueta.nombreEtiqueta())) {
            lista = lista.stream()
                    .filter(t -> t.getEtiqueta() != null && t.getEtiqueta().equals(etiqueta))
                    .toList();
        }

        if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
            String busqueda = textoBusqueda.toLowerCase();
            lista = lista.stream()
                    .filter(t -> t.getNombreTarea().toLowerCase().contains(busqueda))
                    .toList();
        }

        return lista.stream()
                .sorted(Comparator.comparing((Tarea t) -> t.getHoraInicio() == null)
                        .thenComparing(Tarea::getHoraInicio, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Tarea::getNombreTarea))
                .toList();
    }

    //  SUBMETODO 3: Validador booleano de pertenencia de fecha
    private boolean esTareaValidaParaDiaDia(Tarea tarea, LocalDate fechaSeleccionada) {
        return tarea.getFechaInicio() != null && tarea.getFechaFin() != null
                && !fechaSeleccionada.isBefore(tarea.getFechaInicio())
                && !fechaSeleccionada.isAfter(tarea.getFechaFin());
    }

    // SUBMETODO 4: Distribuidor de tareas según sus condiciones de tiempo
    private void ubicarTareaEnCalendarioDiario(Tarea tarea, Pane panelDiaro, VBox panelTareasTodoDiaDiario, MenuPrincipalController jefe) {
        if (tarea.getHoraInicio() != null && tarea.getFechaInicio().isEqual(tarea.getFechaFin())) {
            renderizarTareaDiariaConHoras(tarea, panelDiaro, jefe);
        } else renderizarTareaDiariaTodoElDia(tarea, panelTareasTodoDiaDiario, jefe);

    }

    //  SUBMETODO 5: Algoritmo aislado para calcular carriles libres en el eje X ante colisiones
    private double calcularOffsetLinealX(Pane panelDiaro, int minutosInicio, int duracion) {
        java.util.Objects.requireNonNull(panelDiaro, "El panel diario no puede ser nulo para calcular colisiones");
        double offsetX = 0;
        boolean ocupado = true;

        while (ocupado) {
            ocupado = false;
            for (javafx.scene.Node node : panelDiaro.getChildren()) {
                if (node instanceof Label existente) {
                    boolean choqueY = minutosInicio < (existente.getLayoutY() + existente.getPrefHeight())
                            && (minutosInicio + duracion) > existente.getLayoutY();
                    boolean choqueX = Math.abs(existente.getLayoutX() - offsetX) < 10;

                    if (choqueY && choqueX) {
                        offsetX += 125; // Desplazamos a la derecha si la celda está ocupada en esa franja
                        ocupado = true;
                        break;
                    }
                }
            }
        }
        return offsetX;
    }

    //  SUBMETODO 6: Maquetación y posicionamiento por coordenadas de tareas con hora fija
    private void renderizarTareaDiariaConHoras(Tarea tarea, Pane panelDiaro, MenuPrincipalController jefe) {
        int minutosInicio = (tarea.getHoraInicio().getHour() * 60) + tarea.getHoraInicio().getMinute();
        int duracion = (tarea.getHoraFin().getHour() * 60) + tarea.getHoraFin().getMinute() - minutosInicio;

        Label label = new Label(tarea.getNombreTarea());
        label.setWrapText(true);
        label.setPrefWidth(120);

        double offsetX = calcularOffsetLinealX(panelDiaro, minutosInicio, duracion);

        label.setLayoutY(minutosInicio);
        label.setLayoutX(offsetX);
        label.setPrefHeight(Math.max(duracion, 20));

        if (tarea.getEstadoTarea() != EstadoTarea.EN_PROCESO) label.setOpacity(0.2);

        if (tarea.getEtiqueta() != null && !tarea.getEtiqueta().nombreEtiqueta().equals(TEXT_SIN_ETIQUETA)) {
            label.setStyle(TEXTFX_BACKGROUNDCOLOR + tarea.getEtiqueta().codColor() + "; -fx-text-fill: white; -fx-font-weight: bold;");
        } else label.getStyleClass().add(TEXT_TAREA_SIN_ETIQUETA);

        configurarInteraccionLabelDiario(label, tarea, jefe);
        panelDiaro.getChildren().add(label);
    }

    //  SUBMeTODO 7: Maquetación e inyección en la sección superior del día entero"
    private void renderizarTareaDiariaTodoElDia(Tarea tarea, VBox panelTareasTodoDiaDiario, MenuPrincipalController jefe) {
        java.util.Objects.requireNonNull(panelTareasTodoDiaDiario, "El panel de tareas de todo el día diario no puede ser nulo");

        Label labelTodoDia = new Label(tarea.getNombreTarea());
        labelTodoDia.setMaxWidth(Double.MAX_VALUE);
        labelTodoDia.getStyleClass().add("tarea-todo-dia");
        labelTodoDia.setTooltip(new Tooltip(tarea.mostrarTarea()));

        configurarInteraccionLabelDiario(labelTodoDia, tarea, jefe);
        panelTareasTodoDiaDiario.getChildren().add(labelTodoDia);
    }

    // SUBMETODO 8: Centralización de listeners, menús contextuales y control del botón de ratón
    private void configurarInteraccionLabelDiario(Label label, Tarea tarea, MenuPrincipalController jefe) {
        label.setContextMenu(crearMenuContextual(tarea, jefe));
        label.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                try {
                    View.showTareaVentana(tarea);
                    jefe.mostrarCalendario();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }



    //Para crear los textos qeu aparecen al hacer click derecho sobre la tarea, un menu rapido
    private ContextMenu crearMenuContextual(Tarea tarea,MenuPrincipalController jefe) {
        ContextMenu menu = new ContextMenu();
        MenuItem itemEditar = new MenuItem("✏️ Editar");
        MenuItem itemCompletar = new MenuItem("✅ Completar");
        MenuItem itemBorrar = new MenuItem("🗑️ Borrar");

        itemEditar.setOnAction(e -> {try { View.showTareaVentana(tarea);
            jefe.mostrarCalendario();
        } catch (Exception et) {
            logger.info(et.getMessage());
        }});
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