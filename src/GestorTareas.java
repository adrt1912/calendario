import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GestorTareas {

    private static GestorTareas gestorTareas=new GestorTareas();

    public static GestorTareas getGestorTareas() {
        return gestorTareas;
    }

    private List<String> tareas=new ArrayList<>();

    public void iniciarGestor(){

        Scanner scanner =new Scanner(System.in);
        System.out.println("Buenas, que deseas hacer, especifica el numero porfa" +
                "\n 1.Ver Tareas\n 2.Añadir tarea\n 3.Eliminar tarea\n 4.Modificar tarea\n 5.Leer tareas de un txt\n 6.Salir");

        String action=scanner.nextLine();

        if(action.equals("Ver Tareas")){
            mostrarTareas();
        } else if (action.equals("Añadir tarea")) {
            anadirTarea();
        } else if (action.equals("Eliminar tarea")) {
            eliminarTarea();
        } else if (action.equals("Modificar tarea")) {

        } else if (action.equals("Leer tareas de un txt")) {

        } else if (action.equals("Salir")) {
            salir();
        }
        else {salir();}
    }


    private void mostrarTareas(){
        if(!tareas.isEmpty()){
        System.out.println("Las tareas:");
        tareas.forEach(System.out::println);
        iniciarGestor();
        }
        else{
            System.out.println("La lista de tareas esta vacia");
            iniciarGestor();
        }

    }

    private void anadirTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a añadir");
        String tareaNueva=scanner.nextLine();
        tareas.add(tareaNueva);
        System.out.println("La tarea "+ tareaNueva+"a sido añadida con exito");
        iniciarGestor();
    }

    private void eliminarTarea(){
        Scanner scanner =new Scanner(System.in);

        System.out.println("Dime la tarea a eliminar");
        String tareaEliminar=scanner.nextLine();
        if(tareas.contains(tareaEliminar)){
        tareas.remove(tareaEliminar);
        System.out.println("Tarea "+tareaEliminar+" eliminada");}
        else {
            System.out.println("Esta tarea no esta en la lista");
        }
        iniciarGestor();
    }



    private void salir(){
        System.out.println("Cerrando todo, en esta version se perderan lso datos");
        System.exit(1);
    }

}
