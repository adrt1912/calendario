import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Tarea {

    private String nombreTarea;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTarea estadoTarea;
    private String descripcion;
    private String sitio;
    private LocalTime hora;

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


    public Tarea(String nombreTarea, LocalDate fechaInicio, LocalDate fechaFin, String descripcion, String sitio, LocalTime hora){
        this.nombreTarea=nombreTarea;
        this.fechaInicio=fechaInicio;
        this.fechaFin=fechaFin;
        this.descripcion=descripcion;
        this.sitio=sitio;
        this.hora=hora;
        estadoTarea=EstadoTarea.EN_PROCESO;
        comprobarEstado();
    }

    public void mostrarTarea(){

        if(fechaFin!=null && hora!=null){
        System.out.println(" "+nombreTarea + "se establecio en la fecha "+fechaInicio.toString()+ " termina en la fecha "+fechaFin.toString()+" a las: "+hora.toString()+" lugar: "+sitio
        + "cuya descripcion es:"+descripcion);}
        else {
            System.out.println(" "+nombreTarea + "se establecio en la fecha "+fechaInicio.toString()+" lugar: "+sitio
                    + "cuya descripcion es:"+descripcion);
        }
        comprobarEstado();
    }

    public void completarTarea(){
        estadoTarea=EstadoTarea.COMPLETADA;
    }

    private void comprobarEstado(){

        if(fechaFin!=null && LocalDate.now().isAfter(fechaFin)){
            estadoTarea=EstadoTarea.CADUCADA;
        }

    }

}
