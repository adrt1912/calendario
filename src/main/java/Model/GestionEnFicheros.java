package Model;

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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GestionEnFicheros {

    private static final GestionEnFicheros gestionEnFicheros = new GestionEnFicheros();

    public static GestionEnFicheros getGestionEnFicheros() {return gestionEnFicheros;}

    public File obtenerUltimoBackup(String prefijo) {
        File dir = new File("backups");
        // Filtramos solo los archivos que empiezan por el prefijo (ej: "tareas_backup_")
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefijo) && name.endsWith(".txt"));

        if (files == null || files.length == 0) return null;

        // Ordenamos alfabéticamente (como la fecha está en formato ISO YYYY-MM-DD, funciona perfecto)
        Arrays.sort(files);

        // Devolvemos el último (el más reciente)
        return files[files.length - 1];
    }

    public void leerFicheroTareas() {

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
        String nombreArchivo = "backups/tareas_backup_" + LocalDate.now() + ".txt";

        File archivo = new File(nombreArchivo);
        if (archivo.exists()) {

            try (Scanner lectorFichero = new Scanner(new File(nombreArchivo))) {
                DateTimeFormatter formatoHora = GestorTareas.getGestorTareas().getFormatoHora();

                while (lectorFichero.hasNextLine()) {

                    String titulo = lectorFichero.nextLine();
                    if (titulo.isBlank()) continue;

                    String fechainicstring = lectorFichero.nextLine();
                    LocalDate fechainic = null;
                    if (!fechainicstring.equals("null") && !fechainicstring.isEmpty()) fechainic = LocalDate.parse(fechainicstring);


                    String fechafinstring = lectorFichero.nextLine();
                    LocalDate fechaFin = null;
                    if (!fechafinstring.equals("null") && !fechafinstring.isEmpty()) fechaFin = LocalDate.parse(fechafinstring);

                    String estadoTexto = lectorFichero.nextLine();
                    EstadoTarea estadoTarea = null;
                    if (!estadoTexto.equals("null") && !estadoTexto.isBlank()) {
                        estadoTarea = switch (estadoTexto) {
                            case "COMPLETADA" -> EstadoTarea.COMPLETADA;
                            case "CADUCADA" -> EstadoTarea.CADUCADA;
                            case "EN_PROCESO" -> EstadoTarea.EN_PROCESO;
                            default -> null;
                        };
                    }
                    String descripcion = lectorFichero.nextLine();

                    String sitio = lectorFichero.nextLine();

                    String horaInicTexto = lectorFichero.nextLine();
                    LocalTime horaInicio = null;
                    if (!horaInicTexto.equals("null") && !horaInicTexto.isEmpty()) horaInicio = LocalTime.parse(horaInicTexto, formatoHora);

                    String horaFinTexto = lectorFichero.nextLine();
                    LocalTime horaFin = null;
                    if (!horaFinTexto.equals("null") && !horaFinTexto.isEmpty()) horaFin = LocalTime.parse(horaFinTexto, formatoHora);

                    Periodicidad frecuencia = Periodicidad.valueOf(lectorFichero.nextLine());
                    String idFamilia = lectorFichero.nextLine();

                    String etiquetaText = lectorFichero.nextLine().trim();
                    Etiqueta etiquetaAsignada = GestorTareas.getGestorTareas().getListaEtiquetas().stream().filter(e -> e.nombreEtiqueta() != null && e.nombreEtiqueta().equals(etiquetaText)).findFirst().orElse(null);
                    String idTareaGuardado = lectorFichero.nextLine();

                    Tarea tarea = new Tarea(titulo, fechainic, fechaFin, estadoTarea, descripcion, sitio, horaInicio, horaFin, frecuencia, idFamilia, etiquetaAsignada);
                    if (idTareaGuardado != null && !idTareaGuardado.isEmpty() && !idTareaGuardado.equals("null")) tarea.setIdTarea(idTareaGuardado);

                    GestorTareas.getGestorTareas().aniadirTareaAListaDeDocumento(tarea);
                }
            } catch (Exception ignored) {}
        }
    }

    public void guardarCopiaSeguridadTareas(List<Tarea> listaTareas) {
        File carpeta = new File("backups");
        if (!carpeta.exists()) carpeta.mkdir();
        String nombreArchivo = "backups/tareas_backup_" + LocalDate.now() + ".txt";
        try (FileWriter printWriter = new FileWriter(nombreArchivo);
                PrintWriter pw = new PrintWriter(printWriter) {
                }) {
            for (Tarea tarea : listaTareas) {
                String etiquetaTexto = (tarea.getEtiqueta() != null) ? tarea.getEtiqueta().toString() : "Sin Etiqueta";
                pw.println(tarea.getNombreTarea() + "\n" + tarea.getFechaInicio() + "\n" + tarea.getFechaFin() + "\n" + tarea.getEstadoTarea() + "\n" + tarea.getDescripcion() + "\n" + tarea.getSitio() + "\n" + tarea.getHoraInicio() + "\n" + tarea.getHoraFin() + "\n" + tarea.getFrecuencia() + "\n" + tarea.getIdFamilia() + "\n" + etiquetaTexto + "\n" + tarea.getIdTarea());            }

        } catch (Exception e) {
            System.out.println("Error en la copida de seguridad");
        }
    }

    public void borrarFichero(String nomF) {
        File archivo = new File(nomF);
        archivo.delete();
    }

    public void guardarEtiquetasCopiaSeguridadEtiquetas(List<Etiqueta> listaEtiquetas) {
        File carpeta = new File("backups");
        if (!carpeta.exists()) carpeta.mkdir();

        String nombreArchivo = "backups/etiquetas_backup_" + LocalDate.now() + ".txt";
        try (FileWriter printWriter = new FileWriter(nombreArchivo);
             PrintWriter pw = new PrintWriter(printWriter) {
             }) {
            for (Etiqueta etiqueta : listaEtiquetas) {
                if ("Sin Etiqueta".equalsIgnoreCase(etiqueta.nombreEtiqueta()) || "transparent".equalsIgnoreCase(etiqueta.codColor())) {
                    continue;
                }
                pw.println(etiqueta.nombreEtiqueta() + "\n" + etiqueta.codColor());
            }
        } catch (Exception ignored) {}
    }

    public void leerEtiquetas() {
        String nombreArchivo = "backups/etiquetas_backup_" + LocalDate.now() + ".txt";

        try (Scanner lectorFichero = new Scanner(new File(nombreArchivo))) {
            while (lectorFichero.hasNext()) {
                String nomE = lectorFichero.nextLine();
                String color = lectorFichero.nextLine();
                GestorTareas.getGestorTareas().nuevaEtiqueta(nomE, color);
            }
        } catch (Exception ignored) {}
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
            throw new RuntimeException(e);
        }
    }

    public void exportarAICS(Stage ventana) {
        List<Tarea> listaTareas = GestorTareas.getGestorTareas().getTodasTareas();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Calendario");
        fileChooser.setInitialFileName("Mis_Tareas.ics");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de iCalendar (*.ics)", "*.ics"));

        File archivoDestino = fileChooser.showSaveDialog(ventana);

        // Si el usuario canceló la ventana, el archivo es null. Abortamos la operación.
        if (archivoDestino != null) {

            try (FileWriter printWriter = new FileWriter(archivoDestino);
                 PrintWriter pw = new PrintWriter(printWriter)) {
                pw.println("BEGIN:VCALENDAR");
                pw.println("VERSION:2.0");
                pw.println("PRODID:-//Aday//Gestor de Tareas//ES");
                for (Tarea tarea : listaTareas) {
                    pw.println("BEGIN:VEVENT");
                    pw.println("UID:" + tarea.getIdTarea());
                    pw.println("SUMMARY:" + tarea.getNombreTarea());
                    String fechaInic = tarea.getFechaInicio().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String fechaFin = tarea.getFechaFin().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String horaInic = (tarea.getHoraInicio() != null) ? tarea.getHoraInicio().format(DateTimeFormatter.ofPattern("HHmmss")) : "000000";
                    String horaFin = (tarea.getHoraFin() != null) ? tarea.getHoraFin().format(DateTimeFormatter.ofPattern("HHmmss")) : "235959";
                    String fechaInicCompleta = fechaInic + "T" + horaInic;
                    String fechaFinCompleta = fechaFin + "T" + horaFin;
                    pw.println("DTSTART:" + fechaInicCompleta);
                    pw.println("DTEND:" + fechaFinCompleta);
                    if (tarea.getDescripcion() != null && !tarea.getDescripcion().isBlank())
                        pw.println("DESCRIPTION:" + tarea.getDescripcion());

                    if (tarea.getSitio() != null && !tarea.getSitio().isBlank())
                        pw.println("LOCATION:" + tarea.getSitio());

                    if (tarea.getEstadoTarea() == EstadoTarea.CADUCADA)
                        pw.println("STATUS:CANCELLED"); // Si está caducada, al calendario del móvil le sale tachada
                    else pw.println("STATUS:CONFIRMED"); // Por defecto (EN_PROCESO)
                    pw.println("END:VEVENT");
                }
                pw.println("END:VCALENDAR");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

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
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}