import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class GestorTareas {

    //Se aplica el patron singletone, ya que solo vamos a tener un gestor de tareas
    private static final GestorTareas gestorTareas=new GestorTareas();

    public static GestorTareas getGestorTareas() {
        return gestorTareas;
    }

    private List<Tarea> todasTareas=new ArrayList<>();

    //PAra evitar repetir la llamada al metodo constantemente lo guardamos
    private GestionEnFicheros gestionEnFicheros=GestionEnFicheros.getGestionEnFicheros();

    //Metodo que inicia el gestor
    public void iniciarGestor(){

        //Primero se descargan las tareas guardadas
        gestionEnFicheros.leerFichero("tareas.txt");
        //Se muestra el menu por la terminal, a cambiar en el futuro
        Scanner scanner =new Scanner(System.in);
        System.out.println("""
                Buenas, que deseas hacer, especifica el numero porfa
                 1.Ver Tareas
                 2.Añadir tarea
                 3.Eliminar tarea
                 4.Completar tarea
                 5.Gestionar ficheros
                 6.Modificar tarea\s
                 7.Salir""");

        String action=scanner.nextLine();

        //Segun lo que se pida se hace una cosa u otra
        switch (action) {
            case "Ver Tareas","1" -> mostrarTareas();
            case "Añadir tarea","2" -> anadirTarea();
            case "Eliminar tarea" ,"3"-> eliminarTarea();
            case "Completar tarea" ,"4"-> completarTarea();
            case "Gestionar ficheros","5" -> gestionarFicheros();
            case "Modificar tarea","6" -> modificarTarea();
            case "Salir", "salir" ,"7"-> salir();
            default -> {
                System.out.println("Dime un comando correcto");
                iniciarGestor();
            }
        }
    }

    //Comprueba si la tarea esta ya creada, si no es asi la guarda
    public void añadirTareaALista(Tarea tarea){
        if(todasTareas.stream().noneMatch(t->t.equals(tarea))){
          todasTareas.add(tarea);
        }
    }

//Metodo que muestra las tareas
    private void mostrarTareas(){
        if(!todasTareas.isEmpty()) {
            System.out.println("Las tareas en proceso:");
            List<Tarea> listaMostrar = todasTareas.stream().filter(a -> a.getEstadoTarea() == EstadoTarea.EN_PROCESO).toList();
            for (int i = 0; i < listaMostrar.size(); i++) {
                System.out.print(i + 1 + ":");
                listaMostrar.get(i).mostrarTarea();
            }

            Scanner scanner = new Scanner(System.in);

            System.out.println("Dime que lista de tares quieres ver (Completados o Caducados), o para salir escribir otra cosa");
            String accion = scanner.nextLine();

            if (accion.equals("Completados")) {
                listaMostrar=todasTareas.stream().filter(a->a.getEstadoTarea()==EstadoTarea.COMPLETADA).toList();
                if (!listaMostrar.isEmpty()) {
                    System.out.println("Las tareas en proceso:");
                    for (int i = 0; i < listaMostrar.size(); i++) {
                        System.out.print(i + 1 + ":");
                        listaMostrar.get(i).mostrarTarea();
                    }
                } else {
                    System.out.println("La lista de tareas en finalizadas esta vacia");
                }

            } else if (accion.equals("Caducados")) {
                listaMostrar=todasTareas.stream().filter(a->a.getEstadoTarea()==EstadoTarea.CADUCADA).toList();
                if (!listaMostrar.isEmpty()) {
                    System.out.println("Las tareas en proceso:");
                    for (int i = 0; i < listaMostrar.size(); i++) {
                        System.out.print(i + 1 + ":");
                        listaMostrar.get(i).mostrarTarea();
                    }
                } else {
                    System.out.println("La lista de tareas caducadas esta vacia");
                }
            }
            iniciarGestor();
        }  else{
            System.out.println("La lista de tareas en proceso esta vacia");
        }
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

        Tarea tareaNueva=new Tarea(titulo, LocalDate.now(),fechaFin,EstadoTarea.EN_PROCESO,descripcion,sitio,time);
        añadirTareaALista(tareaNueva);
        gestionEnFicheros.guardarEnFichero(todasTareas);

        System.out.println("La tarea "+ titulo+"a sido añadida con exito");
        iniciarGestor();
    }

    private void eliminarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a eliminar");
        String tareaEliminar=scanner.nextLine();
        Tarea tarea= todasTareas.stream().filter(tarea1 -> tarea1.getNombreTarea().equals(tareaEliminar)).findFirst().orElse(null);
        if(tarea!=null){
            todasTareas.remove(tarea);
        System.out.println("Tarea "+tareaEliminar+" eliminada");}
        else {
            System.out.println("Esta tarea no esta en la lista");
        }
        gestionEnFicheros.guardarEnFichero(todasTareas);
        iniciarGestor();
    }

    private void completarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a marcar");
        String tareaCompletada=scanner.nextLine();
        Tarea tarea= todasTareas.stream().filter(tarea1 -> tarea1.getNombreTarea().equals(tareaCompletada)).findFirst().orElse(null);
        if(tarea!=null){
        tarea.completarTarea();
        System.out.println("La tarea se ha marcado como completada");}
        else {
            System.out.println("La tarea no se encuentra");
        }
        gestionEnFicheros.guardarEnFichero(todasTareas);
        iniciarGestor();
    }

    private void modificarTarea(){

        Scanner scanner =new Scanner(System.in);
        System.out.println("Que tarea se desea cambiar? Introducir el titulo");
        String titulo=scanner.nextLine();
        Tarea tarea=todasTareas.stream().filter(a-> Objects.equals(a.getNombreTarea(), titulo)).findFirst().orElse(null);
        if(tarea==null){System.out.println("No existe esa tarea");}
        else{

            System.out.println("Que se desea cambiar \n 1.Titulo \n 2.FechaFin \n 3.Hora \n 4.Descripcion \n 5.Sitio");
            String accion=scanner.nextLine();
            System.out.println("Especifica el nuevo valor");
            String nuevo=scanner.nextLine();
           try {


            switch (accion){
                case "1","Titulo","titulo" -> tarea.setNombreTarea(nuevo);
                case "2","FechaFin","fechafin","Fechafin","fechaFin" -> tarea.setFechaFin(LocalDate.parse(nuevo,DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                case "3","Hora","hora"->tarea.setHora(LocalTime.parse(nuevo,DateTimeFormatter.ofPattern("HH:mm")));
                case "4","Descripcion","descripcion"->tarea.setDescripcion(nuevo);
                case "5","Sitio","sitio"->tarea.setSitio(nuevo);
            }
            gestionEnFicheros.guardarEnFichero(todasTareas);
        } catch (Exception e) {
               System.out.println("El dato es erroneo");
           }
        }
        iniciarGestor();
    }

    private void gestionarFicheros(){

        System.out.println("""
                Sobre ficheros que desa
                 1.Guardar tareas\s
                 2.Descargar tareas\s
                 3.Limpiar ficheros""");

        Scanner scanner=new Scanner(System.in);
        String accion=scanner.nextLine();
        switch (accion) {
            case "Guardar tareas","1" -> {
                gestionEnFicheros.guardarEnFichero(todasTareas);
            }
            case "Descargar tareas" ,"2"-> {

                System.out.println("Nombre del fichero, o por defecto(no escribir nada)");
                String nomF= scanner.nextLine();
                if(nomF.isEmpty())
                {nomF="tareas.txt";}
                else{
                    nomF=nomF+".txt";
                }
                gestionEnFicheros.leerFichero(nomF);
            }
            case "Limpiar ficheros","3" -> {
                System.out.println("Nombre del fichero, o por defecto(no escribir nada)");
                String nomF= scanner.nextLine();
                if(nomF.isEmpty())
                {nomF="tareas.txt";}
                else{
                    nomF=nomF+".txt";
                }
                gestionEnFicheros.borrarFichero(nomF);
            }
        }
        iniciarGestor();
    }

    private void salir(){
        System.out.println("Cerrando todo");
        System.exit(1);
    }

}
