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

    public List<Tarea> getTodasTareas() {
        return todasTareas;
    }

    private List<Etiqueta> listaEtiquetas=new ArrayList<>();
    //PAra evitar repetir la llamada al metodo constantemente lo guardamos
    private GestionEnFicheros gestionEnFicheros= GestionEnFicheros.getGestionEnFicheros();

    private GestorTareas(){}

    public String mostrarTareasUrgentesHoy(){
//Cogemos solo las tareas en proceso
        StringBuilder taresDevolver= new StringBuilder();
        int numt=0;
        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();
        for (Tarea todasTarea : tareasProcesar) {
            //Si su fecha fin es hoy la mostrara por pantalla
            if (Objects.equals(todasTarea.getFechaFin(), LocalDate.now())) {
               taresDevolver.append(" ").append(todasTarea.getNombreTarea());
               numt++;
               if(todasTarea.getHora()!=null) taresDevolver.append(" a las: ").append(todasTarea.getHora());
            }
        }
        if(numt==0) return "Hoy no tienes tareas pendientes";
        else return "Hoy tienes "+numt+" tareas: "+ taresDevolver.toString();
    }

    public String mostrarTareasUrgentesMañana(){
        StringBuilder taresDevolver= new StringBuilder();
        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();
        int numT=0;
        for(Tarea todasTareaM : tareasProcesar){
            //Si su fecha fin es mañana la mostrara por pantalla
            if (Objects.equals(todasTareaM.getFechaFin(),LocalDate.now().plusDays(1))) {
                taresDevolver.append(" ").append(todasTareaM.getNombreTarea());
                numT++;
                if(todasTareaM.getHora()!=null) taresDevolver.append(" a las: ").append(todasTareaM.getHora());
            }
        }
        if(numT==0) return "Mañana no tienes tareas pendientes";
        else return "Mañana tienes "+numT+" tareas: "+taresDevolver.toString();
    }

    //Metodo que inicia el gestor
    public void iniciarGestor() {
        //Se encarga de la primera carga y de mostrar tareas urgentes
        gestionEnFicheros.leerEtiquetas();

        gestionEnFicheros.leerFichero("tareas.txt");
    }


    //Comprueba si la tarea esta ya creada, si no es asi la guarda
    public void añadirTareaALista(Tarea tarea){
        if(todasTareas.stream().noneMatch(t->t.equals(tarea))){
          todasTareas.add(tarea);
        }
    }

    public Tarea anadirTarea(String titulo,LocalDate fechaFin,String descripcion,String sitio,LocalTime time,Periodicidad frecuencia,String idFamilia,Etiqueta etiqueta){
        Tarea tareaNueva=new Tarea(titulo, LocalDate.now(),fechaFin, EstadoTarea.EN_PROCESO,descripcion,sitio,time,frecuencia,idFamilia,etiqueta);
        añadirTareaALista(tareaNueva);
        gestionEnFicheros.guardarEnFichero(todasTareas);
        return tareaNueva;
    }

    public void eliminarTarea(Tarea tarea){
        todasTareas.remove(tarea);
        gestionEnFicheros.guardarEnFichero(todasTareas);
    }

    public void modificarTarea(Tarea tarea,String titulo,LocalDate fechaFin,String descripcion,String sitio,LocalTime time,Periodicidad frecuencia,EstadoTarea estadoTarea,Etiqueta etiqueta){

        tarea.setNombreTarea(titulo);
        tarea.setFechaFin(fechaFin);
        tarea.setDescripcion(descripcion);
        tarea.setSitio(sitio);
        tarea.setHora(time);
        tarea.setFrecuencia(frecuencia);
        tarea.setEstadoTarea(estadoTarea);
        tarea.setEtiqueta(etiqueta);
        gestionEnFicheros.guardarEnFichero(todasTareas);
    }

    public void eliminarEtiqueta(Etiqueta etiqueta)
    {
        listaEtiquetas.remove(etiqueta);
        gestionEnFicheros.guardarEtiquetas(listaEtiquetas);
    }
    public void nuevaEtiqueta(String color,String nombre){
        Etiqueta etiqueta=new Etiqueta(color,nombre);
        listaEtiquetas.add(etiqueta);
        gestionEnFicheros.guardarEtiquetas(listaEtiquetas);
    }

    public List<Etiqueta> getListaEtiquetas(){return listaEtiquetas;}

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

}
