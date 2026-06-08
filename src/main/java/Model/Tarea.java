package Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Tarea {

    private String nombreTarea;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTarea estadoTarea;
    private String descripcion;
    private String sitio;
    private LocalTime hora;
    private Integer frecuencia;

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

    public Integer getFrecuencia(){return frecuencia;}

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

    public void setFrecuencia(int frecuencia){
        this.frecuencia=frecuencia;
    }


    public Tarea(String nombreTarea, LocalDate fechaInicio, LocalDate fechaFin, EstadoTarea estadoTarea, String descripcion, String sitio, LocalTime hora,String frecuencia){
        this.nombreTarea=nombreTarea;
        this.fechaInicio=fechaInicio;
        this.fechaFin=fechaFin;
        this.estadoTarea=estadoTarea;
        this.descripcion=descripcion;
        this.sitio=sitio;
        this.hora=hora;
        if(frecuencia != null && !frecuencia.isEmpty() && !frecuencia.equals("null")){
        this.frecuencia=Integer.parseInt(frecuencia);}else{
            this.frecuencia=null;
        }
        comprobarEstado();

    }

    public String mostrarTarea(){

        if(fechaFin!=null && hora!=null){
            comprobarEstado();

            return(" "+nombreTarea + " termina el "+ fechaFin +" a las: "+ hora +"\n lugar: "+sitio
        + " descripcion es:"+descripcion);}
        else {
            comprobarEstado();

            return (" "+nombreTarea +" lugar: "+sitio
                    + "cuya descripcion es:"+descripcion);
        }
    }

    public void completarTarea(){
        estadoTarea=EstadoTarea.COMPLETADA;
    }

    private void comprobarEstado(){

        if(fechaFin!=null && LocalDate.now().isAfter(fechaFin)&&estadoTarea.equals(EstadoTarea.EN_PROCESO)){
            estadoTarea=EstadoTarea.CADUCADA;
        }

    }

    @Override
    public boolean equals(Object tarea1) {
        if(this==tarea1){return true;}
        if (tarea1 instanceof Tarea tarea) {
           return Objects.equals(tarea.getFechaFin(),fechaFin)&&
                   Objects.equals(tarea.getFechaInicio(),fechaInicio)&&
                   Objects.equals(tarea.getNombreTarea(),nombreTarea)&&
                   tarea.getEstadoTarea()==estadoTarea&&
                   Objects.equals(tarea.getDescripcion(),descripcion)&&
                   Objects.equals(tarea.getSitio(),sitio)&&
                   Objects.equals(tarea.getHora(),hora);
        }
        return false;

    }
}
