package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.prefs.Preferences;

public class Tarea {

    //Datos de la tarea
    private String nombreTarea;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTarea estadoTarea;
    private String descripcion;
    private String sitio;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Periodicidad frecuencia;

    private String idTarea;
    private String idFamilia;

    private Etiqueta etiqueta;

    public Tarea() {}

    //Getters de la tarea
    public EstadoTarea getEstadoTarea() {
        return estadoTarea;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public String getSitio() {
        return sitio;
    }

    public Periodicidad getFrecuencia() {
        return frecuencia;
    }

    public String getIdTarea() {
        return idTarea;
    }

    public String getIdFamilia() {
        return idFamilia;
    }

    public Etiqueta getEtiqueta() {
        return etiqueta;
    }

    //Setters
    public void setEstadoTarea(EstadoTarea estadoTarea) {
        this.estadoTarea = estadoTarea;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
        comprobarEstado();
    }
    public void setHoraInicio(LocalTime hora) {
        this.horaInicio = hora;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void setSitio(String sitio) {
        this.sitio = sitio;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFrecuencia(Periodicidad frecuencia) {this.frecuencia = frecuencia;}

    public void setIdTarea(String idTarea) {
        this.idTarea = idTarea;
    }

    public void setIdFamilia(String idFamilia) {
        this.idFamilia = idFamilia;
    }

    public void setEtiqueta(Etiqueta etiqueta) {
        this.etiqueta = etiqueta;
    }

    //Constructor
    public Tarea(String nombreTarea, LocalDate fechaInicio, LocalDate fechaFin, EstadoTarea estadoTarea, String descripcion, String sitio, LocalTime horaInicio, LocalTime horaFin, Periodicidad frecuencia,String idFamilia,Etiqueta etiqueta) {
        this.nombreTarea = nombreTarea;
        this.fechaInicio = (fechaInicio != null) ? fechaInicio : LocalDate.now();   if(fechaFin==null)this.fechaFin=fechaInicio;
        this.fechaFin = (fechaFin != null) ? fechaFin : this.fechaInicio;        this.estadoTarea = estadoTarea;
        this.descripcion = descripcion;
        this.sitio = sitio;
        this.horaInicio = horaInicio;
        if(horaInicio!=null&&horaFin==null) horaFin=horaInicio.plusHours(1);
        this.horaFin=horaFin;
        this.frecuencia = frecuencia;
        idTarea= UUID.randomUUID().toString();
        this.idFamilia=idFamilia;

        if(etiqueta==null) this.etiqueta=GestorTareas.getGestorTareas().getEtiquetaNeutra();
        else this.etiqueta=etiqueta;

        comprobarEstado();
    }
    //Obtiene el diccionario y los textos que varian
    private ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        return ResourceBundle.getBundle("textos", new Locale(codIdioma));
    }

    //Devuelve el string de la tarea
    public String mostrarTarea() {
        ResourceBundle resourceBundle=obtenerDiccionario();
        StringBuilder resultado=new StringBuilder();
        resultado.append(resourceBundle.getString("descripcionTareas.titulo")).append(" ").append(nombreTarea).append(" ");
        if(fechaInicio!=null){resultado.append(resourceBundle.getString("descripcionTareas.fechaInicio")).append(" ").append(fechaInicio).append(" \n");
        }
        if(fechaFin!=null){resultado.append(resourceBundle.getString("descripcionTareas.fechaFin")).append(" ").append(fechaFin).append(" \n");}
        if(horaInicio!=null){resultado.append(resourceBundle.getString("descripcionTareas.hora")).append(" ").append(GestorTareas.getGestorTareas().obtenerHoraFormateada(horaInicio)).append(" \n");}
        if(horaFin!=null){resultado.append(resourceBundle.getString("descripcionTareas.horaFin")).append(" ").append(GestorTareas.getGestorTareas().obtenerHoraFormateada(horaFin)).append(" \n");}
        if (sitio != null && !sitio.isBlank()){resultado.append(resourceBundle.getString("descripcionTareas.sitio")).append(" ").append(sitio).append(" \n");}
        if(descripcion!=null&&!descripcion.isBlank()){resultado.append(resourceBundle.getString("descripcionTareas.descripcion")).append(" ").append(descripcion);}
      return resultado.toString();
    }

    //Compureba que no se ha caducado la tarea
    private void comprobarEstado() {
        if (fechaFin == null) return;
        if (LocalDate.now().isAfter(fechaFin) && estadoTarea == EstadoTarea.EN_PROCESO) estadoTarea = EstadoTarea.CADUCADA;
        else if (LocalDate.now().isBefore(fechaFin) && estadoTarea == EstadoTarea.CADUCADA) estadoTarea=EstadoTarea.EN_PROCESO;
    }
//Comprueba si dos tareas son identicas
    @Override
    public boolean equals(Object tarea1) {
        if (this == tarea1) return true;
        if (tarea1 instanceof Tarea tarea) return  Objects.equals(idTarea, tarea.getIdTarea());
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(fechaFin, fechaInicio, nombreTarea, estadoTarea, descripcion, sitio, horaInicio,horaFin);
    }
}