package Model;

import View.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;

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


    private Idiomas idioma;

    public void setIdioma(Idiomas idioma){
        this.idioma=idioma;
    }

    public Idiomas getIdioma() {
        return idioma;
    }

    private List<Etiqueta> listaEtiquetas=new ArrayList<>();
    //PAra evitar repetir la llamada al metodo constantemente lo guardamos
    private GestionEnFicheros gestionEnFicheros= GestionEnFicheros.getGestionEnFicheros();

    private GestorTareas(){
        listaEtiquetas.add(etiquetaNeutra);
    }

    private ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        return ResourceBundle.getBundle("textos", new Locale(codIdioma));
    }
    public String mostrarTareasUrgentesHoy(){
       ResourceBundle resourceBundle=obtenerDiccionario();
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
        if(numt==0) return resourceBundle.getString("gestor.hoySin");
        else return resourceBundle.getString("gestor.hoyCon1")+" "+numt+resourceBundle.getString("gestor.hoyCon2")+" "+ taresDevolver.toString();
    }

    public String mostrarTareasUrgentesMañana(){
        ResourceBundle resourceBundle=obtenerDiccionario();
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
        if(numT==0) return resourceBundle.getString("gestor.mananaSin");
        else return resourceBundle.getString("gestor.mananaCon1")+" "+numT+resourceBundle.getString("gestor.hoyCon2")+" "+taresDevolver.toString();
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

    private Etiqueta etiquetaNeutra=new Etiqueta("Sin Etiqueta","transparent");

    public Etiqueta getEtiquetaNeutra(){
        return etiquetaNeutra;
    }

    public void borrarContenido(){
        listaEtiquetas.clear();
        todasTareas.clear();
        gestionEnFicheros.borrarFichero("tareas.txt");
        gestionEnFicheros.borrarFichero("etiquetas.txt");
        listaEtiquetas.add(etiquetaNeutra);
    }

}
