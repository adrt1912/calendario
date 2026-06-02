import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestorTareas {

    private static GestorTareas gestorTareas=new GestorTareas();

    public static GestorTareas getGestorTareas() {
        return gestorTareas;
    }

    private List<Tarea> tareasEnProceso =new ArrayList<>();
    private List<Tarea> tareasCaducadas=new ArrayList<>();
    private List<Tarea> tareasFinalizadas=new ArrayList<>();

    public void iniciarGestor(){

        comprobarTareas();
        Scanner scanner =new Scanner(System.in);
        System.out.println("Buenas, que deseas hacer, especifica el numero porfa" +
                "\n 1.Ver Tareas\n 2.Añadir tarea\n 3.Eliminar tarea\n 4.Modificar tarea\n 5. Gestionar ficheros\n 6.Salir");

        String action=scanner.nextLine();

        switch (action) {
            case "Ver Tareas" -> mostrarTareas();
            case "Añadir tarea" -> anadirTarea();
            case "Eliminar tarea" -> eliminarTarea();
            case "Completar tarea" -> completarTarea();
            case "Gestionar ficheros" -> gestionarFicheros();
            case "Salir" -> salir();
            default -> {
                System.out.println("Dime un comando correcto");
                iniciarGestor();
            }
        }
    }


    private void mostrarTareas(){
        if(!tareasEnProceso.isEmpty()){
        System.out.println("Las tareas en proceso:");
        for(int i = 0; i< tareasEnProceso.size(); i++){
            System.out.print(i+1+":");
            tareasEnProceso.get(i).mostrarTarea();
            }
        }
        else{
            System.out.println("La lista de tareas en proceso esta vacia");
        }
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime que lista de tares quieres ver (Completados o Caducados), o para salir escribir otra cosa");
        String accion=scanner.nextLine();
        if(accion.equals("Completados")){
            if(!tareasFinalizadas.isEmpty()){
                System.out.println("Las tareas en proceso:");
                for(int i = 0; i< tareasFinalizadas.size(); i++){
                    System.out.print(i+1+":");
                    tareasFinalizadas.get(i).mostrarTarea();
                }
            }
            else{
                System.out.println("La lista de tareas en finalizadas esta vacia");
            }

        }else if(accion.equals("Caducados")){
            if(!tareasCaducadas.isEmpty()){
                System.out.println("Las tareas en proceso:");
                for(int i = 0; i< tareasCaducadas.size(); i++){
                    System.out.print(i+1+":");
                    tareasCaducadas.get(i).mostrarTarea();
                }
            }
            else{
                System.out.println("La lista de tareas caducadas esta vacia");
            }
        }
            iniciarGestor();



    }

    private void anadirTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a añadir, si no se desea rellenar un apartado solo saltar ");
        System.out.println("Titulo:");
        String titulo=scanner.nextLine();
        System.out.println("Fecha fin, introducir en formato dd/mm/yyyy");
        //Se define el tipo de fecha
        LocalDate fechaFin;
       try {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFinString=scanner.nextLine();
         fechaFin=LocalDate.parse(fechaFinString,formato);}
       catch (Exception e) {
           System.out.println("Has puesto mal la fecha, se considerara vacia");
            fechaFin=null;
       }

        System.out.println("Hora, introducir en formato HH:mm");
       LocalTime time;
       try {


        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        String horaTexto=scanner.nextLine();
         time=LocalTime.parse(horaTexto,formatoHora);}
       catch (Exception e) {
           System.out.println("Has puesto mal la hora, se considerara vacia");
           time=null;
       }

        System.out.println("Sitio: ");
        String sitio=scanner.nextLine();

        System.out.println("Descripcion: ");
        String descripcion=scanner.nextLine();

        Tarea tareaNueva=new Tarea(titulo, LocalDate.now(),fechaFin,descripcion,sitio,time);
        tareasEnProceso.add(tareaNueva);
        System.out.println("La tarea "+ titulo+"a sido añadida con exito");
        iniciarGestor();
    }

    private void eliminarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a eliminar");
        String tareaEliminar=scanner.nextLine();
        Tarea tarea= tareasEnProceso.stream().filter(tarea1 -> tarea1.getNombreTarea().equals(tareaEliminar)).findFirst().orElse(null);
        if(tarea!=null){
            tareasEnProceso.remove(tarea);
        System.out.println("Tarea "+tareaEliminar+" eliminada");}
        else {
            System.out.println("Esta tarea no esta en la lista");
        }
        iniciarGestor();
    }

    private void completarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a marcar");
        String tareaCompletada=scanner.nextLine();
        Tarea tarea= tareasEnProceso.stream().filter(tarea1 -> tarea1.getNombreTarea().equals(tareaCompletada)).findFirst().orElse(null);
        if(tarea!=null){
        tarea.completarTarea();
        System.out.println("La tarea se ha marcado como completada");}
        else {
            System.out.println("La tarea no se encuentra");
        }
        
        iniciarGestor();
    }

    private void comprobarTareas(){
        for(int i=tareasEnProceso.size()-1;i>=0;i--){
            Tarea tarea=tareasEnProceso.get(i);
            EstadoTarea estadoTarea=tareasEnProceso.get(i).getEstadoTarea();
            if(estadoTarea.equals(EstadoTarea.COMPLETADA)){
                tareasEnProceso.remove(tarea);
                tareasFinalizadas.add(tarea);
            }
            else if(estadoTarea.equals(EstadoTarea.CADUCADA)){
                tareasEnProceso.remove(tarea);
                tareasCaducadas.add(tarea);
            }
        }
    }

    private void gestionarFicheros(){

        System.out.println("Sobre ficheros que desa" +
                "\n 1.Guardar tareas \n 2.Descargar tareas \n 3.Limpiar ficheros");

        Scanner scanner=new Scanner(System.in);
        String accion=scanner.nextLine();
        switch (accion) {
            case "Guardar tareas" -> {

            }
            case "Descargar tareas" -> {

            }
            case "Limpiar ficheros" -> {

            }
        }
        iniciarGestor();


    }


    private void salir(){
        System.out.println("Cerrando todo, en esta version se perderan lso datos");
        System.exit(1);
    }

}
