package Model;

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

    //Lista con todas las tareas
    private List<Tarea> todasTareas=new ArrayList<>();

    public List<Tarea> getTodasTareas() {
        return todasTareas;
    }

    //Formato de hora
    private DateTimeFormatter formatoHora;

    public DateTimeFormatter getFormatoHora(){return formatoHora;}

    //Idioma
    private Idiomas idioma;

    public void setIdioma(Idiomas idioma){
        this.idioma=idioma;
    }

    public Idiomas getIdioma() {
        return idioma;
    }

    //Lista etiquetas
    private List<Etiqueta> listaEtiquetas=new ArrayList<>();
    //PAra evitar repetir la llamada al metodo constantemente lo guardamos
    private GestionEnFicheros gestionEnFicheros= GestionEnFicheros.getGestionEnFicheros();

    //Al iniciar añade la etiqueta neutra y obtiene el idioma guardado
    private GestorTareas(){
        listaEtiquetas.add(etiquetaNeutra);
        setFormatoHora();
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        this.idioma = Idiomas.desdeCodigo(codIdioma);
    }

    //Establece el formato hora
    private void setFormatoHora() {
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String opcionGuardada = prefs.get("formato_hora", "24h");

        if (opcionGuardada.equals("12h")) {
            formatoHora = DateTimeFormatter.ofPattern("hh:mm a"); // Ejemplo: 06:30 PM
        } else {
            formatoHora = DateTimeFormatter.ofPattern("HH:mm");   // Ejemplo: 18:30
        }
    }

    //obtiene el ResourceBundle del idioma guardado
    public ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        Idiomas idiomaSeleccionado = Idiomas.desdeCodigo(codIdioma);
        setIdioma(idiomaSeleccionado);
        return ResourceBundle.getBundle("textos", new Locale(codIdioma));
    }

    //Se encarga de devolver el texto con tareas de hoy
    public String mostrarTareasUrgentesHoy(){
       ResourceBundle resourceBundle=obtenerDiccionario();
//Cogemos solo las tareas en proceso
        StringBuilder taresDevolver= new StringBuilder();
        int numt=0;
        //Se obtienen las tareas en proceso de hoy
        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();
        List<Tarea> listTareaEscribir=tareasProcesar.stream().filter(tarea -> tarea.getFechaFin().equals(LocalDate.now())).toList();

        for (Tarea todasTarea : listTareaEscribir) {
            //Si su fecha fin es hoy la mostrara por pantalla
               taresDevolver.append(" ").append(todasTarea.getNombreTarea());
               numt++;
               if(todasTarea.getHoraInicio()!=null) taresDevolver.append(" a las: ").append(todasTarea.getHoraInicio());
                if(numt< listTareaEscribir.size()) taresDevolver.append(" y");

        }
        if(numt==0) return resourceBundle.getString("gestor.hoySin");
        else if(numt==1) return resourceBundle.getString("gestor.hoyCon1")+" "+numt+" "+resourceBundle.getString("gestor.hoyCon2")+" "+ taresDevolver.toString();
        else return resourceBundle.getString("gestor.hoyCon1")+" "+numt+" "+resourceBundle.getString("gestor.hoyCon2")+"s: "+ taresDevolver.toString();
    }

    //Para mostrar las tareas de mañana
    public String mostrarTareasUrgentesMañana(){
        ResourceBundle resourceBundle=obtenerDiccionario();
        StringBuilder taresDevolver= new StringBuilder();
        List<Tarea> tareasProcesar=todasTareas.stream().filter(a->a.getEstadoTarea()== EstadoTarea.EN_PROCESO).toList();
        int numT=0;
            //Si su fecha fin es mañana la mostrara por pantalla
            List<Tarea> listTareaEscribir=tareasProcesar.stream().filter(tarea -> tarea.getFechaFin().equals(LocalDate.now().plusDays(1))).toList();
        for(Tarea todasTareaM : listTareaEscribir){
                taresDevolver.append(" ").append(todasTareaM.getNombreTarea());
                numT++;
                if(todasTareaM.getHoraInicio()!=null) taresDevolver.append(" a las: ").append(todasTareaM.getHoraInicio());
                if(numT< listTareaEscribir.size()) taresDevolver.append(" y ");
            }

        if(numT==0) return resourceBundle.getString("gestor.mananaSin");
        else if(numT==1) return resourceBundle.getString("gestor.mananaCon1")+" "+numT+" "+resourceBundle.getString("gestor.hoyCon2")+" "+taresDevolver.toString();
        else return resourceBundle.getString("gestor.mananaCon1")+" "+numT+" "+resourceBundle.getString("gestor.hoyCon2")+"s: "+taresDevolver.toString();
    }

    //Metodo que inicia el gestor
    public void iniciarGestor() {
        //Se encarga de la primera carga y de mostrar tareas urgentes
        ConexionBD.getConexionBD().crearTablasSiNoExisten();
        listaEtiquetas.clear();
        listaEtiquetas.add(etiquetaNeutra);
        todasTareas.clear();
        ConexionBD.getConexionBD().cargarDatosDeBD();
    }

    //Comprueba si la tarea esta ya creada, si no es asi la guarda
    public void añadirTareaALista(Tarea tarea){
        if(todasTareas.stream().noneMatch(t->t.equals(tarea))){
          todasTareas.add(tarea);
        }
    }

    //Crea la nueva tarea con los datos recibidos
    public Tarea anadirTarea(String titulo,LocalDate fechaFin,String descripcion,String sitio,LocalTime timeInicial,LocalTime horaFin,Periodicidad frecuencia,String idFamilia,Etiqueta etiqueta){
        Tarea tareaNueva=new Tarea(titulo, LocalDate.now(),fechaFin, EstadoTarea.EN_PROCESO,descripcion,sitio,timeInicial,horaFin,frecuencia,idFamilia,etiqueta);
        // Comprobamos cuántas tareas hay antes de intentar añadirla
        int tamañoAntes = todasTareas.size();
        añadirTareaALista(tareaNueva);
        // Solo la guardamos en BD si de verdad ha entrado en la lista (no era un clon)
        if(todasTareas.size() > tamañoAntes) ConexionBD.getConexionBD().guardarTarea(tareaNueva);
        return tareaNueva;
    }

    //Elimina la tarea
    public void eliminarTarea(Tarea tarea){
        ConexionBD.getConexionBD().borrarTarea(tarea.getIdTarea());
        todasTareas.removeIf(t->t.getIdTarea().equals(tarea.getIdTarea()));
    }

    //Modifica la teare por medio de setters
    public void modificarTarea(Tarea tarea,String titulo,LocalDate fechaFin,String descripcion,String sitio,LocalTime time,Periodicidad frecuencia,EstadoTarea estadoTarea,Etiqueta etiqueta){
        tarea.setNombreTarea(titulo);
        tarea.setFechaFin(fechaFin);
        tarea.setDescripcion(descripcion);
        tarea.setSitio(sitio);
        tarea.setHoraInicio(time);
        tarea.setFrecuencia(frecuencia);
        tarea.setEstadoTarea(estadoTarea);
        tarea.setEtiqueta(etiqueta);
        ConexionBD.getConexionBD().guardarTarea(tarea);
        //gestionEnFicheros.guardarEnFichero(todasTareas);
    }

    //Borra la etiqueta
    public void eliminarEtiqueta(Etiqueta etiqueta) {
        //Todas las tareas con esa etiquera se les cambia a la "Sin etiqueta"
        for (Tarea t : todasTareas) {
            if (t.getEtiqueta() != null && t.getEtiqueta().getNombreEtiqueta().equals(etiqueta.getNombreEtiqueta())) {
                t.setEtiqueta(etiquetaNeutra);
                ConexionBD.getConexionBD().guardarTarea(t);
            }
        }
        listaEtiquetas.remove(etiqueta);
        ConexionBD.getConexionBD().borrarEtiqueta(etiqueta.getNombreEtiqueta());
        //gestionEnFicheros.guardarEtiquetas(listaEtiquetas);
    }
    //Crea una nueva etiqueta
    public void nuevaEtiqueta(String nombre,String color){
        Etiqueta etiqueta=new Etiqueta(nombre,color);
        listaEtiquetas.add(etiqueta);
        ConexionBD.getConexionBD().guardarEtiqueta(etiqueta);
        //gestionEnFicheros.guardarEtiquetas(listaEtiquetas);
    }

    public List<Etiqueta> getListaEtiquetas(){return listaEtiquetas;}

    private Etiqueta etiquetaNeutra=new Etiqueta("Sin Etiqueta","transparent");

    public Etiqueta getEtiquetaNeutra(){
        return etiquetaNeutra;
    }

    //Borra los datos guardados
    public void borrarContenido(){
        GestionEnFicheros.getGestionEnFicheros().guardarCopiaSeguridadTareas(todasTareas);
        GestionEnFicheros.getGestionEnFicheros().guardarEtiquetasCopiaSeguridadEtiquetas(listaEtiquetas);
        listaEtiquetas.clear();
        todasTareas.clear();
        gestionEnFicheros.borrarFichero("tareas.txt");
        gestionEnFicheros.borrarFichero("etiquetas.txt");
        ConexionBD.getConexionBD().vaciarBaseDeDatos();
        listaEtiquetas.add(etiquetaNeutra);
    }

    //Devuelve el string de la hora segun el formato seleccionado
    public String obtenerHoraFormateada(LocalTime hora) {
        if (hora == null) return "";
        String formatoElegido = Preferences.userNodeForPackage(View.view.class).get("formato_hora", "24h");
        DateTimeFormatter formateador;
        if (formatoElegido.equals("12h")) {
            formateador = DateTimeFormatter.ofPattern("hh:mm a"); // 06:30 PM
        } else {
            formateador = DateTimeFormatter.ofPattern("HH:mm");   // 18:30
        }
        return hora.format(formateador);
    }
    public void verificarTareasHoy() {
        LocalDate hoy = LocalDate.now();
        for (Tarea t : todasTareas) { // Asegúrate de tener acceso a tu lista
            if (t.getFechaFin() != null && t.getFechaFin().equals(hoy)) {
                NotificadorDeTareas.mostrarNotificacion("Tarea para hoy", "Tienes pendiente: " + t.getNombreTarea(),t);
            }
        }
    }
}