package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GestorTareas {

    //Se aplica el patron singletone, ya que solo vamos a tener un gestor de tareas
    private static final GestorTareas gestorTareas=new GestorTareas();

    public static GestorTareas getGestorTareas() {
        return gestorTareas;
    }

    private List<Tarea> todasTareas=new ArrayList<>();

    //PAra evitar repetir la llamada al metodo constantemente lo guardamos
    private GestionEnFicheros gestionEnFicheros= GestionEnFicheros.getGestionEnFicheros();

    private GestorTareas(){}

    public String mostrarTareasUrgentesHoy(){
//Cogemos solo las tareas en proceso
        StringBuilder taresDevolver= new StringBuilder();

        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();
        for (Tarea todasTarea : tareasProcesar) {
            //Si su fecha fin es hoy la mostrara por pantalla
            if (Objects.equals(todasTarea.getFechaFin(), LocalDate.now())) {

               taresDevolver.append(" ").append(todasTarea.getNombreTarea()).append(" a las: ").append(todasTarea.getHora());
            }
        }
        return taresDevolver.toString();
    }

    public String mostrarTareasUrgentesMañana(){
        StringBuilder taresDevolver= new StringBuilder();        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();

        for(Tarea todasTareaM : tareasProcesar){
            //Si su fecha fin es mañana la mostrara por pantalla
            if (Objects.equals(todasTareaM.getFechaFin(),LocalDate.now().plusDays(1))) {
                taresDevolver.append(" ").append(todasTareaM.getNombreTarea()).append(" a las: ").append(todasTareaM.getHora());
            }
        }
        return taresDevolver.toString();
    }


    //Solo se ejecuta una vez, pero no puede ir en el constructor ya que al usar la otra clase, esa otra es null
    boolean primerGestor=true;

    //Metodo que inicia el gestor
    public void iniciarGestor(){
        if(primerGestor){
            //Se encarga de la primera carga y de mostrar tareas urgentes
        gestionEnFicheros.leerFichero("tareas.txt");
        primerGestor=false;
        System.out.println("\n \n \n");
        }
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
        todasTareas.sort(Comparator.comparing(Tarea::getEstadoTarea).thenComparing(Tarea::getFechaFin,Comparator.nullsLast(Comparator.naturalOrder())));
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
    public void mostrarTareas(){
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
                listaMostrar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.COMPLETADA).toList();
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
                listaMostrar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.CADUCADA).toList();
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
        }  else{
            System.out.println("La lista de tareas en proceso esta vacia");
        }
        iniciarGestor();

    }

    public void anadirTarea(){
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


        System.out.println("Quieres que sea periodica, si es asi introducir frecuencia");
            String frecuencia=scanner.nextLine();

        Tarea tareaNueva=new Tarea(titulo, LocalDate.now(),fechaFin, EstadoTarea.EN_PROCESO,descripcion,sitio,time,frecuencia);
        añadirTareaALista(tareaNueva);
        gestionEnFicheros.guardarEnFichero(todasTareas);

        System.out.println("La tarea "+ titulo+"a sido añadida con exito");
        iniciarGestor();
    }

    public void eliminarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a eliminar");
        String tareaEliminar=scanner.nextLine();
        List<Tarea> listTareaActuar=buscadorInteligente(tareaEliminar);
        if(!listTareaActuar.isEmpty()) {
            for(int i = 0; i < listTareaActuar.size(); i++) {
                System.out.println(i + 1 + ": " + listTareaActuar.get(i).getNombreTarea());
            }
            System.out.println("Dime el numero de la tarea a eliminar y 0 para cancelar");
            int numEliminar = scanner.nextInt();
            scanner.nextLine();
            if (numEliminar != 0) {
                Tarea tarea = listTareaActuar.get(numEliminar - 1);
                todasTareas.remove(tarea);
                System.out.println("Java.Controller.Model.Tarea " + tareaEliminar + " eliminada");
            } else {
                System.out.println("Esta tarea no se ha eliminado");
            }
            gestionEnFicheros.guardarEnFichero(todasTareas);
            iniciarGestor();
        }else {
            System.out.println("No se encuentra la tarea");
            iniciarGestor();
        }
        }

    public void completarTarea() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Dime la tarea a marcar");
        String tareaCompletada = scanner.nextLine();
        List<Tarea> listTareaActuar = buscadorInteligente(tareaCompletada).stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();

        if (!listTareaActuar.isEmpty()) {
            for (int i = 0; i < listTareaActuar.size(); i++) {
                System.out.println(i + 1 + ": " + listTareaActuar.get(i).getNombreTarea());
            }
            System.out.println("Dime el numero de la tarea a eliminar y 0 para cancelar");
            int numModificar = scanner.nextInt();
            scanner.nextLine();
            if (numModificar != 0) {
                Tarea tarea = listTareaActuar.get(numModificar - 1);
                tarea.completarTarea();
                System.out.println("La tarea se ha marcado como completada");
                if(tarea.getFrecuencia()!=null){
                    todasTareas.add(new Tarea(tarea.getNombreTarea(),tarea.getFechaInicio(),tarea.getFechaFin().plusDays(tarea.getFrecuencia()), EstadoTarea.EN_PROCESO,tarea.getDescripcion(),tarea.getSitio(),tarea.getHora(), tarea.getFrecuencia().toString()));
                }
            } else {
                System.out.println("La tarea no se ha modificado");
            }
            gestionEnFicheros.guardarEnFichero(todasTareas);
            iniciarGestor();
        } else {
            System.out.println("No se encuentra la tarea");
            iniciarGestor();
        }
    }

    public void modificarTarea(){

        Scanner scanner =new Scanner(System.in);
        System.out.println("Que tarea se desea cambiar? Introducir el titulo");
        String titulo=scanner.nextLine();
        List<Tarea> tareas=buscadorInteligente(titulo);
        if(!tareas.isEmpty()) {
            for (int i = 0; i < tareas.size(); i++) {
                System.out.println(i + 1 + ": " + tareas.get(i).getNombreTarea());
            }
            System.out.println("Dimer el numero de la tarea a modificar y 0 para cancelar");
            int numMod = scanner.nextInt();
            scanner.nextLine();
            if (numMod != 0) {
                Tarea tarea = tareas.get(numMod);
                System.out.println("Que se desea cambiar \n 1.Titulo \n 2.FechaFin \n 3.Hora \n 4.Descripcion \n 5.Sitio");
                String accion = scanner.nextLine();
                System.out.println("Especifica el nuevo valor");
                String nuevo = scanner.nextLine();
                try {
                    switch (accion) {
                        case "1", "Titulo", "titulo" -> tarea.setNombreTarea(nuevo);
                        case "2", "FechaFin", "fechafin", "Fechafin", "fechaFin" ->
                                tarea.setFechaFin(LocalDate.parse(nuevo, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        case "3", "Hora", "hora" ->
                                tarea.setHora(LocalTime.parse(nuevo, DateTimeFormatter.ofPattern("HH:mm")));
                        case "4", "Descripcion", "descripcion" -> tarea.setDescripcion(nuevo);
                        case "5", "Sitio", "sitio" -> tarea.setSitio(nuevo);
                    }
                    gestionEnFicheros.guardarEnFichero(todasTareas);
                } catch (Exception e) {
                    System.out.println("El dato es erroneo");
                }

            }
        }
        iniciarGestor();
    }

    public void gestionarFicheros(){

        System.out.println("""
                Sobre ficheros que desa
                 1.Guardar tareas\s
                 2.Descargar tareas\s
                 3.Limpiar ficheros""");

        Scanner scanner=new Scanner(System.in);
        String accion=scanner.nextLine();
        switch (accion) {
            case "Guardar tareas","1" -> gestionEnFicheros.guardarEnFichero(todasTareas);
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

    private List<Tarea> buscadorInteligente (String palabraBuscar){
        List<Tarea> listaTareaCoincidentes=new ArrayList<>();

        for (Tarea todasTarea : todasTareas) {
            if (todasTarea.getNombreTarea().toLowerCase().contains(palabraBuscar.toLowerCase())) {
                listaTareaCoincidentes.add(todasTarea);
            }
        }
        return listaTareaCoincidentes;
    }

    private void salir(){
        System.out.println("Cerrando todo");
        System.exit(1);
    }

}
