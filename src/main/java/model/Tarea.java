package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

    private String idTarea = UUID.randomUUID().toString();
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

    public void setEtiqueta(Etiqueta etiqueta) {
        this.etiqueta = etiqueta;
    }

    //Constructor
    public Tarea(TareaDatos tareaDatos, EstadoTarea estadoTarea) {
        this.nombreTarea = tareaDatos.titulo();
        this.estadoTarea = estadoTarea;
        this.descripcion = tareaDatos.descripcion();
        this.sitio = tareaDatos.sitio();
        this.idFamilia = tareaDatos.idFamilia();

        this.fechaInicio = (tareaDatos.fechaInicio() != null) ? tareaDatos.fechaInicio() : LocalDate.now(ZoneId.systemDefault());
        this.fechaFin = (tareaDatos.fechaFin() != null) ? tareaDatos.fechaFin() : this.fechaInicio;

        this.horaInicio = tareaDatos.timeInicial();
        LocalTime hFin = tareaDatos.horaFin();
        if (this.horaInicio != null && hFin == null) hFin = this.horaInicio.plusHours(1);

        this.horaFin = hFin;

        this.frecuencia = (tareaDatos.frecuencia() != null) ? tareaDatos.frecuencia() : Periodicidad.NUNCA;

        if (tareaDatos.etiqueta() == null) this.etiqueta = GestorTareas.getGestorTareas().getEtiquetaNeutra();
        else this.etiqueta = tareaDatos.etiqueta();


        comprobarEstado();
    }
    //Obtiene el diccionario y los textos que varian
    private ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(Tarea.class);
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
        if (LocalDate.now(ZoneId.systemDefault()).isAfter(fechaFin) && estadoTarea == EstadoTarea.EN_PROCESO) estadoTarea = EstadoTarea.CADUCADA;
        else if (LocalDate.now(ZoneId.systemDefault()).isBefore(fechaFin) && estadoTarea == EstadoTarea.CADUCADA) estadoTarea=EstadoTarea.EN_PROCESO;
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
        return Objects.hash(idTarea);
    }
}