package model;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class GestionEnFicheros {

    private static final GestionEnFicheros gestionEnFicheros = new GestionEnFicheros();

    public static GestionEnFicheros getGestionEnFicheros() {return gestionEnFicheros;}

    private static final String TEXT_BACK_UP ="backups";
    Logger logger = Logger.getLogger(getClass().getName());


    public File obtenerUltimoBackup(String prefijo) {
        File dir = new File(TEXT_BACK_UP);
        // Filtramos solo los archivos que empiezan por el prefijo (ej: "tareas_backup_")
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefijo) && name.endsWith(".txt"));

        if (files == null || files.length == 0) return null;

        // Ordenamos alfabéticamente (como la fecha está en formato ISO YYYY-MM-DD, funciona perfecto)
        Arrays.sort(files);

        // Devolvemos el último (el más reciente)
        return files[files.length - 1];
    }


        /*Los ficheros tienen la siguiente estructura
        1.Titulo
           fecha inic
           fechafin
           estadoTarea
           descripcion
           sitio
           hora inicio
           hora fin
           frecuencia
           etiqueta
        2. Titulo...
        Si alguno va vacio se salta de linea
         */
        public void leerFicheroTareas() {
            String nombreArchivo = "backups/tareas_backup_" + LocalDateTime.now(ZoneId.systemDefault()) + ".txt";
            File archivo = new File(nombreArchivo);

            if (!archivo.exists()) {
                return;
            }

            // 🚀 CORREGIDO: El método principal ahora solo coordina la apertura del flujo y el bucle
            try (Scanner lectorFichero = new Scanner(archivo)) {
                DateTimeFormatter formatoHora = GestorTareas.getGestorTareas().getFormatoHora();

                while (lectorFichero.hasNextLine()) {
                    procesarSiguienteTareaFichero(lectorFichero, formatoHora);
                }
            } catch (Exception e) {
                logger.info("Advertencia leyendo el fichero de copias de seguridad de tareas: "+ e);
            }
        }

//  SUBMeTODO 1: Procesa el bloque de 13 líneas de una sola tarea de forma aislada
        private void procesarSiguienteTareaFichero(Scanner lector, DateTimeFormatter formatoHora) {
            String titulo = lector.nextLine();
            if (titulo.isBlank()) return;

            // Extracción limpia delegando en los parseadores seguros de abajo
            LocalDate fechainic = parsearFecha(lector.nextLine());
            LocalDate fechaFin = parsearFecha(lector.nextLine());

            String estadoTexto = lector.nextLine();
            EstadoTarea estadoTarea = esCadenaValida(estadoTexto) ? parsearEstado(estadoTexto) : null;

            String descripcion = lector.nextLine();
            String sitio = lector.nextLine();

            LocalTime horaInicio = parsearHora(lector.nextLine(), formatoHora);
            LocalTime horaFin = parsearHora(lector.nextLine(), formatoHora);

            String frecuenciaStr = lector.nextLine();
            Periodicidad frecuencia = esCadenaValida(frecuenciaStr) ? Periodicidad.valueOf(frecuenciaStr) : null;

            String idFamilia = lector.nextLine();
            String etiquetaText = lector.nextLine().trim();
            Etiqueta etiquetaAsignada = buscarEtiquetaPorNombre(etiquetaText);

            String idTareaGuardado = lector.nextLine();

            // Reconstrucción usando el record TareaDatos y el constructor limpio
            TareaDatos datos = new TareaDatos(titulo, fechainic, fechaFin, descripcion, sitio, horaInicio, horaFin, frecuencia, idFamilia, etiquetaAsignada);
            Tarea tarea = new Tarea(datos, estadoTarea);

            if (esCadenaValida(idTareaGuardado)) tarea.setIdTarea(idTareaGuardado);

            GestorTareas.getGestorTareas().aniadirTareaAListaDeDocumento(tarea);
        }

//  AYUDANTE 1: Centraliza la triple verificación repetitiva de cadenas de texto
        private boolean esCadenaValida(String str) {
            return str != null && !str.isBlank() && !"null".equals(str);
        }

//  AYUDANTE 2: Conversión segura de fechas de texto a LocalDate
        private LocalDate parsearFecha(String fechaStr) {
            return esCadenaValida(fechaStr) ? LocalDate.parse(fechaStr) : null;
        }

// AYUDANTE 3: Conversión segura de horas usando el formato activo del gestor
        private LocalTime parsearHora(String horaStr, DateTimeFormatter formato) {
            return esCadenaValida(horaStr) ? LocalTime.parse(horaStr, formato) : null;
        }

//  AYUDANTE 4: Mapeo de enums simplificado fuera del flujo principal
        private EstadoTarea parsearEstado(String estadoTexto) {
            return switch (estadoTexto) {
                case "COMPLETADA" -> EstadoTarea.COMPLETADA;
                case "CADUCADA" -> EstadoTarea.CADUCADA;
                case "EN_PROCESO" -> EstadoTarea.EN_PROCESO;
                default -> null;
            };
        }

//  AYUDANTE 5: Filtro de flujo de etiquetas aislado
        private Etiqueta buscarEtiquetaPorNombre(String nombreEtiqueta) {
            return GestorTareas.getGestorTareas().getListaEtiquetas().stream()
                    .filter(e -> e.nombreEtiqueta() != null && e.nombreEtiqueta().equals(nombreEtiqueta))
                    .findFirst()
                    .orElse(null);
        }

    public void guardarCopiaSeguridadTareas(List<Tarea> listaTareas) {
        File carpeta = new File(TEXT_BACK_UP);
        if (!carpeta.exists()) carpeta.mkdir();
        String nombreArchivo = "backups/tareas_backup_" +  LocalDateTime.now(ZoneId.systemDefault()) + ".txt";
        try (FileWriter printWriter = new FileWriter(nombreArchivo);
                PrintWriter pw = new PrintWriter(printWriter) {
                }) {
            for (Tarea tarea : listaTareas) {
                String etiquetaTexto = (tarea.getEtiqueta() != null) ? tarea.getEtiqueta().toString() : "Sin Etiqueta";
                pw.println(tarea.getNombreTarea() + "\n" + tarea.getFechaInicio() + "\n" + tarea.getFechaFin() + "\n" + tarea.getEstadoTarea() + "\n" + tarea.getDescripcion() + "\n" + tarea.getSitio() + "\n" + tarea.getHoraInicio() + "\n" + tarea.getHoraFin() + "\n" + tarea.getFrecuencia() + "\n" + tarea.getIdFamilia() + "\n" + etiquetaTexto + "\n" + tarea.getIdTarea());            }

        } catch (Exception e) {
            logger.info("Error en la copida de seguridad");
        }
    }

    public void borrarFichero(String nomF) {
        File archivo = new File(nomF);
        if (!archivo.delete()) logger.info("No se pudo eliminar el archivo físico del disco: {}"+ nomF);
    }

    public void guardarEtiquetasCopiaSeguridadEtiquetas(List<Etiqueta> listaEtiquetas) {
        File carpeta = new File(TEXT_BACK_UP);
        if (!carpeta.exists()) carpeta.mkdir();

        String nombreArchivo = "backups/etiquetas_backup_" +  LocalDateTime.now(ZoneId.systemDefault()) + ".txt";
        try (FileWriter printWriter = new FileWriter(nombreArchivo);
             PrintWriter pw = new PrintWriter(printWriter) {
             }) {
            for (Etiqueta etiqueta : listaEtiquetas) {
                if ("Sin Etiqueta".equalsIgnoreCase(etiqueta.nombreEtiqueta()) || "transparent".equalsIgnoreCase(etiqueta.codColor())) {
                    continue;
                }
                pw.println(etiqueta.nombreEtiqueta() + "\n" + etiqueta.codColor());
            }
        } catch (Exception e) {
            logger.info("Advertencia guardando etiquetas en el fichero: "+e.getMessage());

        }
    }

    public void leerEtiquetas() {
        String nombreArchivo = "backups/etiquetas_backup_" +  LocalDateTime.now(ZoneId.systemDefault()) + ".txt";

        try (Scanner lectorFichero = new Scanner(new File(nombreArchivo))) {
            while (lectorFichero.hasNext()) {
                String nomE = lectorFichero.nextLine();
                String color = lectorFichero.nextLine();
                GestorTareas.getGestorTareas().nuevaEtiqueta(nomE, color);
            }
        } catch (Exception e) {
            logger.info("Advertencia leyendo etiquetas el fichero: "+e.getMessage());

        }
    }

    public void exportarACSV() {

        List<Tarea> listaTareas = GestorTareas.getGestorTareas().getTodasTareas();
        try (FileWriter printWriter = new FileWriter("archivoCSVTareas.csv");
             PrintWriter pw = new PrintWriter(printWriter) {
             }) {
            pw.println("Titulo;Descripcion;Estado;FechaFin;Hora;Etiqueta");
            for (Tarea tarea : listaTareas) {
                String titulo = tarea.getNombreTarea();
                String desc = tarea.getDescripcion() != null ? tarea.getDescripcion() : "";
                String estado = tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : "";
                String fecha = tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : "";
                String horaInicio = tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : "";
                String horaFin = tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : "";
                String etiqueta = tarea.getEtiqueta() != null ? tarea.getEtiqueta().nombreEtiqueta() : "";

                pw.println(titulo + ";" + desc + ";" + estado + ";" + fecha + ";" + horaInicio + ";" + horaFin + ";" + etiqueta);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error crítico al procesar y guardar la nueva tarea en el sistema", e);        }
    }
    public void exportarAICS(Stage ventana) {
        List<Tarea> listaTareas = GestorTareas.getGestorTareas().getTodasTareas();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Calendario");
        fileChooser.setInitialFileName("Mis_Tareas.ics");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de iCalendar (*.ics)", "*.ics"));

        File archivoDestino = fileChooser.showSaveDialog(ventana);

        if (archivoDestino == null) return;

        try (FileWriter printWriter = new FileWriter(archivoDestino);
             PrintWriter pw = new PrintWriter(printWriter)) {

            pw.println("BEGIN:VCALENDAR");
            pw.println("VERSION:2.0");
            pw.println("PRODID:-//Aday//Gestor de Tareas//ES");

            for (Tarea tarea : listaTareas) {
                escribirEventoICS(pw, tarea);
            }

            pw.println("END:VCALENDAR");

        } catch (Exception e) {
            throw new IllegalStateException("Error crítico al procesar y guardar el archivo iCalendar de exportación", e);
        }
    }

    //  SUBMeTODO 1: Procesa la conversión y escritura de una única tarea aislada en formato VEVENT
    private void escribirEventoICS(PrintWriter pw, Tarea tarea) {
        pw.println("BEGIN:VEVENT");
        pw.println("UID:" + tarea.getIdTarea());
        pw.println("SUMMARY:" + tarea.getNombreTarea());

        String fechaInic = tarea.getFechaInicio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fechaFin = tarea.getFechaFin().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String horaInic = (tarea.getHoraInicio() != null)
                ? tarea.getHoraInicio().format(DateTimeFormatter.ofPattern("HHmmss")) : "000000";
        String horaFin = (tarea.getHoraFin() != null)
                ? tarea.getHoraFin().format(DateTimeFormatter.ofPattern("HHmmss")) : "235959";

        pw.println("DTSTART:" + fechaInic + "T" + horaInic);
        pw.println("DTEND:" + fechaFin + "T" + horaFin);

        if (esCampoValido(tarea.getDescripcion())) pw.println("DESCRIPTION:" + tarea.getDescripcion());

        if (esCampoValido(tarea.getSitio())) pw.println("LOCATION:" + tarea.getSitio());

        String estado = (tarea.getEstadoTarea() == EstadoTarea.CADUCADA) ? "CANCELLED" : "CONFIRMED";
        pw.println("STATUS:" + estado);

        pw.println("END:VEVENT");
    }

    // AYUDANTE: Centraliza de forma limpia la verificación de campos de texto vacíos
    private boolean esCampoValido(String campo) {
        return campo != null && !campo.isBlank();
    }

    private  String nombrAR;
    public void elegirArchivoICSLEER(Stage ventana){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Calendario");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de iCalendar (*.ics)", "*.ics"));

        File archivoSeleccionado = fileChooser.showOpenDialog(ventana);
        if (archivoSeleccionado != null) nombrAR = archivoSeleccionado.getAbsolutePath();
         else nombrAR = null;
    }

    public void leerArchivoICS() {

        if (nombrAR != null) {
            try (FileInputStream fin = new FileInputStream(nombrAR)) {
                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar = builder.build(fin);

                // Iteramos sobre todos los eventos encontrados
                for (Component component : calendar.getComponents(Component.VEVENT)) {

                    // 1. Extraer título informacion
                    String descripcion = component.getProperty(Property.DESCRIPTION)
                            .map(Property::getValue)
                            .orElse("");

                    String resumen = component.getProperty(Property.SUMMARY)
                            .map(Property::getValue)
                            .orElse("Tarea importada");

                    String fechaInicioStr = component.getProperty(Property.DTSTART)
                            .map(Property::getValue)
                            .orElse(null);

                        // 2. Extraer DTEND (Fecha y hora fin)
                    String fechaFinStr = component.getProperty(Property.DTEND)
                            .map(Property::getValue)
                            .orElse(fechaInicioStr); // Si no hay fecha fin, usamos la de inicio como fallback

                    if (fechaInicioStr != null) {
                        // Definimos el formateador. Nota: el formato ical suele ser siempre el mismo
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss['Z']");

                        LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr, formatter);
                        LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr, formatter);

                        // 4. Creamos el objeto Tarea
                        Tarea nuevaTarea = new Tarea();
                        nuevaTarea.setNombreTarea(resumen);
                        nuevaTarea.setDescripcion(descripcion);
                        nuevaTarea.setEstadoTarea(EstadoTarea.EN_PROCESO);

                        // Guardamos inicio
                        nuevaTarea.setFechaInicio(fechaInicio.toLocalDate());
                        nuevaTarea.setHoraInicio(fechaInicio.toLocalTime());

                        // Guardamos fin
                        nuevaTarea.setFechaFin(fechaFin.toLocalDate());
                        nuevaTarea.setHoraFin(fechaFin.toLocalTime());

                        GestorTareas.getGestorTareas().agregarTarea(nuevaTarea);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Error crítico al procesar y guardar la nueva tarea en el sistema", e);            }
        }
    }
}