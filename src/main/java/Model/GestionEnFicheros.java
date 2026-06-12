package Model;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class GestionEnFicheros {

    private static final GestionEnFicheros gestionEnFicheros=new GestionEnFicheros();

    public static GestionEnFicheros getGestionEnFicheros() {
        return gestionEnFicheros;
    }

    public void leerFichero(String nombreFichero){

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
        File archivo = new File("tareas.txt");
        if(archivo.exists()){

        try (
                Scanner lectorFichero=new Scanner(new File(nombreFichero))){
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

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

    public void guardarEnFichero(List<Tarea> listaTareas){

        try (
            FileWriter printWriter=new FileWriter("tareas.txt");
            PrintWriter pw=new PrintWriter(printWriter){})
        {
            for (Tarea tarea : listaTareas){
                pw.println(tarea.getNombreTarea() + "\n" + tarea.getFechaInicio() + "\n" + tarea.getFechaFin() + "\n" + tarea.getEstadoTarea() + "\n" + tarea.getDescripcion() + "\n" + tarea.getSitio() + "\n" + tarea.getHora() + "\n"+tarea.getFrecuencia() +"\n"+tarea.getIdFamilia()+"\n"+tarea.getEtiqueta());
            }

        } catch (Exception e) {
            System.out.println("ALgo fallo");
        }
    }

    public void borrarFichero(String nomF){
        File archivo=new File(nomF);
       archivo.delete();
    }

    public void guardarEtiquetas(List<Etiqueta> listaEtiquetas){
        try (
                FileWriter printWriter=new FileWriter("etiquetas.txt");
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
    public void leerEtiquetas(){
        try (
                Scanner lectorFichero=new Scanner(new File("etiquetas.txt"))){
            while (lectorFichero.hasNext()) {
                String nomE = lectorFichero.nextLine();
                String color = lectorFichero.nextLine();
                GestorTareas.getGestorTareas().nuevaEtiqueta(nomE,color);
            }
        } catch (Exception e) {
        }
    }

    public void exportarACSV(){}
}