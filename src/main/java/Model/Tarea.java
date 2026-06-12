package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.prefs.Preferences;

public class Tarea {

    private String nombreTarea;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTarea estadoTarea;
    private String descripcion;
    private String sitio;
    private LocalTime hora;
    private Periodicidad frecuencia;

    private String idTarea;
    private String idFamilia;

    private Etiqueta etiqueta;
    //Getters
    public EstadoTarea getEstadoTarea() {
        return estadoTarea;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalTime getHora() {
        return hora;
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
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
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
    public Tarea(String nombreTarea, LocalDate fechaInicio, LocalDate fechaFin, EstadoTarea estadoTarea, String descripcion, String sitio, LocalTime hora, Periodicidad frecuencia,String idFamilia,Etiqueta etiqueta) {
        this.nombreTarea = nombreTarea;
        this.fechaInicio = fechaInicio;
        if(fechaFin==null)this.fechaFin=LocalDate.now();
        else this.fechaFin = fechaFin;
        this.estadoTarea = estadoTarea;
        this.descripcion = descripcion;
        this.sitio = sitio;
        this.hora = hora;
        this.frecuencia = frecuencia;
        idTarea= UUID.randomUUID().toString();
        this.idFamilia=idFamilia;

        if(etiqueta==null) this.etiqueta=GestorTareas.getGestorTareas().getEtiquetaNeutra();
        else this.etiqueta=etiqueta;

        comprobarEstado();
    }
    private ResourceBundle obtenerDiccionario() {
        Preferences prefs = Preferences.userNodeForPackage(View.view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        return ResourceBundle.getBundle("textos", new Locale(codIdioma));
    }

    //Devuelve el string de la tarea
    public String mostrarTarea() {
        ResourceBundle resourceBundle=obtenerDiccionario();
        String resultado=resourceBundle.getString("descripcionTareas.titulo")+" "+nombreTarea+" ";
        if(fechaFin!=null){resultado=resultado+resourceBundle.getString("descripcionTareas.fechaFin")+" "+fechaFin+" ";}
        if(hora!=null){resultado=resultado+resourceBundle.getString("descripcionTareas.hora")+" "+hora+" ";}
        if(sitio!=null){resultado=resultado+resourceBundle.getString("descripcionTareas.sitio")+" "+sitio+" ";}
        if(descripcion!=null){resultado=resultado+resourceBundle.getString("descripcionTareas.descripcion")+" "+descripcion;}
      return resultado;
    }

    //Compureba que no se ha caducado la tarea
    private void comprobarEstado() {
        if (fechaFin == null) {
            return;
        }
        if (LocalDate.now().isAfter(fechaFin) && estadoTarea == EstadoTarea.EN_PROCESO) {
            estadoTarea = EstadoTarea.CADUCADA;
        } else if (LocalDate.now().isBefore(fechaFin) && estadoTarea == EstadoTarea.CADUCADA) {
            estadoTarea=EstadoTarea.EN_PROCESO;
        }
    }
//Comprueba si dos tareas son identicas
    @Override
    public boolean equals(Object tarea1) {
        if (this == tarea1) {
            return true;
        }
        if (tarea1 instanceof Tarea tarea) {
            return Objects.equals(tarea.getFechaFin(), fechaFin) &&
                    Objects.equals(tarea.getFechaInicio(), fechaInicio) &&
                    Objects.equals(tarea.getNombreTarea(), nombreTarea) &&
                    tarea.getEstadoTarea() == estadoTarea &&
                    Objects.equals(tarea.getDescripcion(), descripcion) &&
                    Objects.equals(tarea.getSitio(), sitio) &&
                    Objects.equals(tarea.getHora(), hora);
        }
        return false;
    }
}