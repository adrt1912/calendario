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

                String frecuencia=lectorFichero.nextLine();

                Tarea tarea = new Tarea(titulo,fechainic,fechaFin, estadoTarea,descripcion,sitio,time,frecuencia);
                GestorTareas.getGestorTareas().añadirTareaALista(tarea);

            }
        } catch (Exception e) {
           System.out.println(e.getMessage());
        }
    }

    public void guardarEnFichero(List<Tarea> listaTareas){

        try (
            FileWriter printWriter=new FileWriter("tareas.txt");
            PrintWriter pw=new PrintWriter(printWriter){})
        {
            for (Tarea tarea : listaTareas){
                pw.println(tarea.getNombreTarea() + "\n" + tarea.getFechaInicio() + "\n" + tarea.getFechaFin() + "\n" + tarea.getEstadoTarea() + "\n" + tarea.getDescripcion() + "\n" + tarea.getSitio() + "\n" + tarea.getHora() + "\n"+tarea.getFrecuencia() );
            }
                System.out.println("Tareas guardadas correctamente");

        } catch (Exception e) {
            System.out.println("ALgo fallo");
        }
    }

    public void borrarFichero(String nomF){
        File archivo=new File(nomF);

        if(archivo.delete()){
            System.out.println("Se borro correctamente");
        }else{System.out.println("No se pudo borrar");}

    }
}