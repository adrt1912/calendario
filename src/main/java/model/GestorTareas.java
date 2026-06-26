package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static utils.SeguridadUtils.encriptarPIN;

public class GestorTareas {

    // Se aplica el patrón Singleton, ya que solo vamos a tener un gestor de tareas
    private static final GestorTareas gestorTareas = new GestorTareas();

    private static final Logger logger = LoggerFactory.getLogger(GestorTareas.class);

    public static GestorTareas getGestorTareas() {
        return gestorTareas;
    }

    // Lista con todas las tareas
    private final List<Tarea> todasTareas = new ArrayList<>();

    public List<Tarea> getTodasTareas() {
        return todasTareas;
    }

    // Formato de hora
    private DateTimeFormatter formatoHora;

    public DateTimeFormatter getFormatoHora() {
        return formatoHora;
    }

    // Idioma
    private Idiomas idioma;

    public void setIdioma(Idiomas idioma) {
        this.idioma = idioma;
    }

    public Idiomas getIdioma() {
        return idioma;
    }

    private int idUsuarioLogueado = -1; //-1 nadie logueado

    public int getIdUsuarioLogueado() {
        return idUsuarioLogueado;
    }

    public void setIdUsuarioLogueado(int idUsuarioLogueado) {
        this.idUsuarioLogueado = idUsuarioLogueado;
    }

    // Lista etiquetas
    private final List<Etiqueta> listaEtiquetas = new ArrayList<>();

    // Para evitar repetir la llamada al mEtodo constantemente lo guardamos
    private final GestionEnFicheros gestionEnFicheros = GestionEnFicheros.getGestionEnFicheros();

    public List<Etiqueta> getListaEtiquetas() {
        return listaEtiquetas;
    }

    private final Etiqueta etiquetaNeutra = new Etiqueta("Sin Etiqueta", "transparent");

    public Etiqueta getEtiquetaNeutra() {
        return etiquetaNeutra;
    }

    private static final String TEXT_HOY2 = "gestor.hoyCon2";

    private final List<Tarea> listaTareasAvisadasT = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true); // Evita que el hilo bloquee el cierre de la app
        return thread;
    });

    // Al iniciar añade la etiqueta neutra y obtiene el idioma guardado
    private GestorTareas() {
        listaEtiquetas.add(etiquetaNeutra);
        setFormatoHora();

        Preferences prefs = Preferences.userNodeForPackage(View.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        this.idioma = Idiomas.desdeCodigo(codIdioma);

        // Lanzamos el planificador de comprobación de tareas cada 30 segundos de forma segura
        scheduler.scheduleAtFixedRate(this::comprobarAlertasDeTareas, 0, 30, TimeUnit.SECONDS);
    }

    // 🚀 CORREGIDO: Lógica extraída del constructor para fulminar la complejidad cognitiva y corregir los operadores de hora
    private void comprobarAlertasDeTareas() {
        try {
            List<Tarea> listaTareasComprobar = todasTareas.stream()
                    .filter(tarea -> !listaTareasAvisadasT.contains(tarea))
                    .toList();

            LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
            LocalTime ahora = LocalTime.now(ZoneId.systemDefault());

            for (Tarea tarea : listaTareasComprobar) {
                if (tarea.getFechaFin() != null && hoy.equals(tarea.getFechaFin())
                        && tarea.getEstadoTarea() == EstadoTarea.EN_PROCESO
                        && tarea.getHoraInicio() != null
                        && !ahora.isBefore(tarea.getHoraInicio())) { // 🔓 BUG ARREGLADO: Agrupa la lógica temporal de forma infalible

                    listaTareasAvisadasT.add(tarea);
                    javafx.application.Platform.runLater(() ->
                            NotificadorDeTareas.mostrarNotificacion(
                                    "¡Tarea Pendiente ahora!",
                                    "Es hora de: " + tarea.getNombreTarea(),
                                    tarea
                            )
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error en el hilo de comprobación de notificaciones: ", e);
        }
    }

    // Establece el formato hora
    private void setFormatoHora() {
        Preferences prefs = Preferences.userNodeForPackage(View.class);
        String opcionGuardada = prefs.get("formato_hora", "24h");

        if (opcionGuardada.equals("12h")) {
            formatoHora = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        } else {
            formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        }
    }

    // Obtiene el ResourceBundle del idioma guardado
    public ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(View.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        Idiomas idiomaSeleccionado = Idiomas.desdeCodigo(codIdioma);
        setIdioma(idiomaSeleccionado);
        return ResourceBundle.getBundle("textos", new Locale(codIdioma));
    }

    // Se encarga de devolver el texto con tareas de hoy
    public String mostrarTareasUrgentesHoy() {
        ResourceBundle resourceBundle = obtenerDiccionario();
        StringBuilder taresDevolver = new StringBuilder();
        int numt = 0;

        List<Tarea> tareasProcesar = todasTareas.stream().filter(a -> a.getEstadoTarea() == EstadoTarea.EN_PROCESO).toList();
        List<Tarea> listTareaEscribir = tareasProcesar.stream().filter(tarea -> tarea.getFechaFin().equals(LocalDate.now(ZoneId.systemDefault()))).toList();

        for (Tarea todasTarea : listTareaEscribir) {
            taresDevolver.append(" ").append(todasTarea.getNombreTarea());
            numt++;
            if (todasTarea.getHoraInicio() != null) {
                taresDevolver.append(" a las: ").append(todasTarea.getHoraInicio());
            }
            if (numt < listTareaEscribir.size()) {
                taresDevolver.append(" y");
            }
        }

        switch (numt) {
            case 0 -> { return resourceBundle.getString("gestor.hoySin"); }
            case 1 -> { return resourceBundle.getString("gestor.hoyCon1") + " " + numt + " " + resourceBundle.getString(TEXT_HOY2) + " " + taresDevolver; }
            default -> { return resourceBundle.getString("gestor.hoyCon1") + " " + numt + " " + resourceBundle.getString(TEXT_HOY2) + "s: " + taresDevolver; }
        }
    }

    // Para mostrar las tareas de mañana
    public String mostrarTareasUrgentesManiana() {
        ResourceBundle resourceBundle = obtenerDiccionario();
        StringBuilder taresDevolver = new StringBuilder();
        List<Tarea> tareasProcesar = todasTareas.stream().filter(a -> a.getEstadoTarea() == EstadoTarea.EN_PROCESO).toList();
        int numT = 0;

        List<Tarea> listTareaEscribir = tareasProcesar.stream().filter(tarea -> tarea.getFechaFin().equals(LocalDate.now(ZoneId.systemDefault()).plusDays(1))).toList();
        for (Tarea todasTareaM : listTareaEscribir) {
            taresDevolver.append(" ").append(todasTareaM.getNombreTarea());
            numT++;
            if (todasTareaM.getHoraInicio() != null) {
                taresDevolver.append(" a las: ").append(todasTareaM.getHoraInicio());
            }
            if (numT < listTareaEscribir.size()) {
                taresDevolver.append(" y ");
            }
        }

        switch (numT) {
            case 0 -> { return resourceBundle.getString("gestor.mananaSin"); }
            case 1 -> { return resourceBundle.getString("gestor.mananaCon1") + " " + numT + " " + resourceBundle.getString(TEXT_HOY2) + " " + taresDevolver; }
            default -> { return resourceBundle.getString("gestor.mananaCon1") + " " + numT + " " + resourceBundle.getString(TEXT_HOY2) + "s: " + taresDevolver; }
        }
    }

    // Método que inicia el gestor
    public void iniciarGestor() {
        ConexionBD.getConexionBD().crearTablasSiNoExisten();
        listaEtiquetas.clear();
        listaEtiquetas.add(etiquetaNeutra);
        todasTareas.clear();
        ConexionBD.getConexionBD().cargarDatosDeBD(idUsuarioLogueado);
    }

    // 1. USADO POR LA BD / FICHEROS: Para cargar los datos al arrancar sin duplicar en RAM
    public void aniadirTareaAListaDeDocumento(Tarea tarea) {
        if (todasTareas.stream().noneMatch(t -> t.equals(tarea))) {
            todasTareas.add(tarea);
        }
    }

    // 3. USADO POR EL FORMULARIO DE LA VISTA: Crea el objeto utilizando el constructor limpio
    public Tarea anadirTarea(TareaDatos datos) {
        Tarea tareaNueva = new Tarea(datos, EstadoTarea.EN_PROCESO);
        agregarTarea(tareaNueva);
        return tareaNueva;
    }

    // 2. USADO POR EL IMPORTADOR ICS: Añade a la RAM (controlando duplicados por ID) y guarda en la BD
    public void agregarTarea(Tarea tarea) {
        if (todasTareas.stream().noneMatch(t -> t.getIdTarea().equals(tarea.getIdTarea()))) {
            todasTareas.add(tarea);
            ConexionBD.getConexionBD().guardarTarea(tarea);
        }
    }

    // Elimina la tarea
    public void eliminarTarea(Tarea tarea) {
        ConexionBD.getConexionBD().borrarTarea(tarea.getIdTarea());
        todasTareas.removeIf(t -> t.getIdTarea().equals(tarea.getIdTarea()));
    }

    // Modifica la tarea por medio de setters
    public void modificarTarea(Tarea tarea, TareaDatos datos, EstadoTarea estadoTarea) {
        tarea.setNombreTarea(datos.titulo());
        tarea.setFechaInicio(datos.fechaInicio());
        tarea.setFechaFin(datos.fechaFin());
        tarea.setDescripcion(datos.descripcion());
        tarea.setSitio(datos.sitio());
        tarea.setHoraInicio(datos.timeInicial());
        tarea.setHoraFin(datos.horaFin());
        tarea.setFrecuencia(datos.frecuencia());
        tarea.setEtiqueta(datos.etiqueta());
        tarea.setEstadoTarea(estadoTarea);
        ConexionBD.getConexionBD().guardarTarea(tarea);
    }

    // Borra la etiqueta
    public void eliminarEtiqueta(Etiqueta etiqueta) {
        for (Tarea t : todasTareas) {
            if (t.getEtiqueta() != null && t.getEtiqueta().nombreEtiqueta().equals(etiqueta.nombreEtiqueta())) {
                t.setEtiqueta(etiquetaNeutra);
                ConexionBD.getConexionBD().guardarTarea(t);
            }
        }
        listaEtiquetas.remove(etiqueta);
        ConexionBD.getConexionBD().borrarEtiqueta(etiqueta.nombreEtiqueta());
    }

    // Crea una nueva etiqueta
    public void nuevaEtiqueta(String nombre, String color) {
        Etiqueta etiqueta = new Etiqueta(nombre, color);
        listaEtiquetas.add(etiqueta);
        ConexionBD.getConexionBD().guardarEtiqueta(etiqueta);
    }

    // Borra los datos guardados
    public void borrarContenido() {
        GestionEnFicheros.getGestionEnFicheros().guardarCopiaSeguridadTareas(todasTareas);
        GestionEnFicheros.getGestionEnFicheros().guardarEtiquetasCopiaSeguridadEtiquetas(listaEtiquetas);
        listaEtiquetas.clear();
        todasTareas.clear();
        gestionEnFicheros.borrarFichero("tareas.txt");
        gestionEnFicheros.borrarFichero("etiquetas.txt");
        ConexionBD.getConexionBD().vaciarBaseDeDatos();
        listaEtiquetas.add(etiquetaNeutra);
    }

    // 🚀 CORREGIDO: Centraliza y reutiliza el formateador interno en lugar de instanciar uno nuevo cada vez
    public String obtenerHoraFormateada(LocalTime hora) {
        if (hora == null) return "";
        setFormatoHora();
        return hora.format(formatoHora);
    }

    public void verificarTareasHoy() {
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        for (Tarea t : todasTareas) {
            if (t.getFechaFin() != null && t.getFechaFin().equals(hoy)) {
                NotificadorDeTareas.mostrarNotificacion("Tarea para hoy", "Tienes pendiente: " + t.getNombreTarea(), t);
            }
        }
    }

    public boolean verificarHash(String pinIntroducido, String hashGuardado) {
        if (pinIntroducido == null || hashGuardado == null) return false;
        String hashDelIntento = encriptarPIN(pinIntroducido);
        return hashDelIntento.equals(hashGuardado);
    }

    public void cerrarSesion() {
        listaEtiquetas.clear();
        todasTareas.clear();
        idUsuarioLogueado = -1;
        claveCifradoActiva = null;
    }

    private javax.crypto.spec.SecretKeySpec claveCifradoActiva;

    public javax.crypto.spec.SecretKeySpec getClaveCifradoActiva() {
        return claveCifradoActiva;
    }

    public void setClaveCifradoActiva(javax.crypto.spec.SecretKeySpec clave) {
        this.claveCifradoActiva = clave;
    }
}