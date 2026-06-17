package Model;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GestionEnFicheros {

    private static final GestionEnFicheros gestionEnFicheros=new GestionEnFicheros();

    public static GestionEnFicheros getGestionEnFicheros() {
        return gestionEnFicheros;
    }

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

    public void leerFicheroTareas(File ultimoTareas){

        /*Los ficheros tienen la siguiente estructura
        1.Titulo
           fecha inic
           fechafin
           estadoTarea
           descripcion
           sitio
           hora
        2. Titulo...
        Si alguno va vacio se salta de linea
         */
        String nombreArchivo = "backups/tareas_backup_" + LocalDate.now() + ".txt";

        File archivo = new File(nombreArchivo);
        if(archivo.exists()){

        try (
                Scanner lectorFichero=new Scanner(new File(nombreArchivo))){
            DateTimeFormatter formatoHora =GestorTareas.getGestorTareas().getFormatoHora();

            while(lectorFichero.hasNextLine()) {

                String titulo=lectorFichero.nextLine();
                if(titulo.isBlank()){continue;}

                String fechainicstring = lectorFichero.nextLine();
                LocalDate fechainic=null;
                if(!fechainicstring.equals("null")&&!fechainicstring.isEmpty()){
                 fechainic = LocalDate.parse(fechainicstring);}

                String fechafinstring = lectorFichero.nextLine();
                LocalDate fechaFin=null;
                if(!fechafinstring.equals("null")&&!fechafinstring.isEmpty()){
                 fechaFin = LocalDate.parse(fechafinstring);}

                String estadoTexto= lectorFichero.nextLine();
                EstadoTarea estadoTarea=null;
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

                String horaTexto = lectorFichero.nextLine();
                LocalTime time=null;
                if(!horaTexto.equals("null")&&!horaTexto.isEmpty()){
                 time = LocalTime.parse(horaTexto, formatoHora);}

                Periodicidad frecuencia= Periodicidad.valueOf(lectorFichero.nextLine());
                String idFamilia=lectorFichero.nextLine();

                String etiquetaText=lectorFichero.nextLine().trim();
                Etiqueta etiquetaAsignada=GestorTareas.getGestorTareas().getListaEtiquetas().stream().filter(e ->e.getNombreEtiqueta()!=null && e.getNombreEtiqueta().equals(etiquetaText)).findFirst().orElse(null);

                Tarea tarea = new Tarea(titulo,fechainic,fechaFin, estadoTarea,descripcion,sitio,time,frecuencia,idFamilia,etiquetaAsignada);
                GestorTareas.getGestorTareas().añadirTareaALista(tarea);

            }
        } catch (Exception e) {
           System.out.println(e.getMessage());
        }
    }
    }

    public void guardarCopiaSeguridadTareas(List<Tarea> listaTareas){
        File carpeta = new File("backups");
        if (!carpeta.exists()) carpeta.mkdir();
        String nombreArchivo = "backups/tareas_backup_" + LocalDate.now() + ".txt";
        try (
            FileWriter printWriter=new FileWriter(nombreArchivo);
            PrintWriter pw=new PrintWriter(printWriter){})
        {
            for (Tarea tarea : listaTareas){
                pw.println(tarea.getNombreTarea() + "\n" + tarea.getFechaInicio() + "\n" + tarea.getFechaFin() + "\n" + tarea.getEstadoTarea() + "\n" + tarea.getDescripcion() + "\n" + tarea.getSitio() + "\n" + tarea.getHora() + "\n"+tarea.getFrecuencia() +"\n"+tarea.getIdFamilia()+"\n"+tarea.getEtiqueta());
            }

        } catch (Exception e) {
            System.out.println("Error en la copida de seguridad");
        }
    }

    public void borrarFichero(String nomF){
        File archivo=new File(nomF);
       archivo.delete();
    }

    public void guardarEtiquetasCopiaSeguridadEtiquetas(List<Etiqueta> listaEtiquetas){
        File carpeta = new File("backups");
        if (!carpeta.exists()) carpeta.mkdir();

        String nombreArchivo = "backups/etiquetas_backup_" + LocalDate.now() + ".txt";
        try (FileWriter printWriter=new FileWriter(nombreArchivo);
                PrintWriter pw=new PrintWriter(printWriter){}){
            for(Etiqueta etiqueta : listaEtiquetas){
                if ("Sin Etiqueta".equalsIgnoreCase(etiqueta.getNombreEtiqueta()) || "transparent".equalsIgnoreCase(etiqueta.getCodColor())) {
                    continue;
                }
                pw.println(etiqueta.getNombreEtiqueta()+"\n"+etiqueta.getCodColor());
            }
        } catch (Exception e) {

        }
    }

    public void leerEtiquetas(File ultimasEtiquetas){
        String nombreArchivo = "backups/etiquetas_backup_" + LocalDate.now() + ".txt";

        try (
                Scanner lectorFichero=new Scanner(new File(nombreArchivo))){
            while (lectorFichero.hasNext()) {
                String nomE = lectorFichero.nextLine();
                String color = lectorFichero.nextLine();
                GestorTareas.getGestorTareas().nuevaEtiqueta(nomE,color);
            }
        } catch (Exception e) {
        }
    }

    public void exportarACSV(){

        List<Tarea> listaTareas=GestorTareas.getGestorTareas().getTodasTareas();
        try (
                FileWriter printWriter=new FileWriter("archivoCSVTareas.csv");
                PrintWriter pw=new PrintWriter(printWriter){}){
            pw.println("Titulo;Descripcion;Estado;FechaFin;Hora;Etiqueta");
            for( Tarea tarea : listaTareas){
                String titulo = tarea.getNombreTarea();
                String desc = tarea.getDescripcion() != null ? tarea.getDescripcion() : "";
                String estado = tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : "";
                String fecha = tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : "";
                String hora = tarea.getHora() != null ? tarea.getHora().toString() : "";
                String etiqueta = tarea.getEtiqueta() != null ? tarea.getEtiqueta().getNombreEtiqueta() : "";

                pw.println(titulo + ";" + desc + ";" + estado + ";" + fecha + ";" + hora + ";" + etiqueta);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}