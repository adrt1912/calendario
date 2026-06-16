package Model;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class conexionBD {

    private static conexionBD conexionBD;
    private conexionBD(){
        conexionBD=this;
    }

    public static conexionBD getConexionBD() {
        if (conexionBD == null) {
            conexionBD = new conexionBD();
        }
        return conexionBD;
    }

    private static final String URL = "jdbc:sqlite:base_tareas.db";


        public void cargarDatosDeBD(){

            String op1="select * from Etiqueta";
            String op="select * from Tarea";
            DateTimeFormatter formatoHora =GestorTareas.getGestorTareas().getFormatoHora();


            try (Connection c= DriverManager.getConnection(URL);
                 PreparedStatement ps=c.prepareStatement(op);
                 PreparedStatement ps1=c.prepareStatement(op1);
                 )
            {
                ResultSet rs1=ps1.executeQuery();
                while (rs1.next()){
                    String nomE = rs1.getString("nombreEtiqueta");
                    String color = rs1.getString("codColor");
                    Etiqueta nuevaEtiqueta = new Etiqueta(nomE, color);
                    GestorTareas.getGestorTareas().getListaEtiquetas().add(nuevaEtiqueta);
                }

                //En cada caso se ha de comprobar que no sean nulos
                ResultSet rs=ps.executeQuery();
                while(rs.next()) {
                    String titulo = rs.getString("Titulo");

                    String fechaInicStr = rs.getString("FechaInicio");
                    LocalDate fechainic = (fechaInicStr != null && !fechaInicStr.isBlank() && !fechaInicStr.equals("null"))
                            ? LocalDate.parse(fechaInicStr) : null;

                    String fechaFinS = rs.getString("FechaFin");
                    LocalDate fechaFin = (fechaFinS != null && !fechaFinS.isBlank() && !fechaFinS.equals("null"))
                            ? LocalDate.parse(fechaFinS) : null;

                    String estadoTareaS = rs.getString("EstadoTarea");
                    EstadoTarea estadoTarea = null;
                    if (estadoTareaS != null && !estadoTareaS.isBlank() && !estadoTareaS.equals("null")) {
                        estadoTarea = EstadoTarea.valueOf(estadoTareaS);
                    }

                    String horaStr = rs.getString("Hora");
                    LocalTime time = (horaStr != null && !horaStr.isBlank() && !horaStr.equals("null"))
                            ? LocalTime.parse(horaStr, formatoHora) : null;

                    String frecuenciaStr = rs.getString("Frecuencia");
                    Periodicidad frecuencia = (frecuenciaStr != null && !frecuenciaStr.isBlank() && !frecuenciaStr.equals("null"))
                            ? Periodicidad.valueOf(frecuenciaStr) : null;

                    String descripcion = rs.getString("Descripcion");
                    String sitio = rs.getString("Sitio");
                    String idTarea = rs.getString("idTarea");
                    String idFamilia = rs.getString("idFamilia");
                    String etiqueta = rs.getString("Etiqueta");

                    Etiqueta etiquetaAsignada = null;
                    if (etiqueta != null) {
                        etiquetaAsignada = GestorTareas.getGestorTareas().getListaEtiquetas().stream()
                                .filter(e -> e.getNombreEtiqueta() != null && e.getNombreEtiqueta().equals(etiqueta))
                                .findFirst()
                                .orElse(null);
                    }

                    Tarea tarea = new Tarea(titulo, fechainic, fechaFin, estadoTarea, descripcion, sitio, time, frecuencia, idFamilia, etiquetaAsignada);
                    tarea.setIdTarea(idTarea);

                    GestorTareas.getGestorTareas().añadirTareaALista(tarea);
                }
                } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        public void guardarEnBD(){

            String opTareas = "INSERT OR REPLACE INTO Tarea (Titulo, FechaInicio, FechaFin, EstadoTarea, Hora, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            String opEtiquetas = "INSERT OR REPLACE INTO Etiqueta (codColor, nombreEtiqueta) VALUES (?,?)";

            try (Connection c=DriverManager.getConnection(URL);
            PreparedStatement psTareas=c.prepareStatement(opTareas);
            PreparedStatement psEtiquetas=c.prepareStatement(opEtiquetas);
            ){
                List<Etiqueta> listaEtiquetas=GestorTareas.getGestorTareas().getListaEtiquetas();
                List<Tarea> listaTareas=GestorTareas.getGestorTareas().getTodasTareas();

                for(Etiqueta etiqueta : listaEtiquetas){
                    psEtiquetas.setString(1,etiqueta.getCodColor());
                    psEtiquetas.setString(2,etiqueta.getNombreEtiqueta());
                    psEtiquetas.executeUpdate();
                }
                for (Tarea tarea : listaTareas){

                    // Protegemos los datos sensibles a nulos
                    psTareas.setString(1, tarea.getNombreTarea());
                    psTareas.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
                    psTareas.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
                    psTareas.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null); // Mejor usar .name() para el Enum
                    psTareas.setString(5, tarea.getHora() != null ? tarea.getHora().toString() : null);
                    psTareas.setString(6, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);

                    psTareas.setString(7, tarea.getDescripcion());
                    psTareas.setString(8, tarea.getSitio());
                    psTareas.setString(9, tarea.getIdTarea());
                    psTareas.setString(10, tarea.getIdFamilia());
                    psTareas.setString(11, tarea.getEtiqueta() != null ? tarea.getEtiqueta().getNombreEtiqueta() : null);                    psTareas.executeUpdate();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }

        public void borrarTarea(String idTarea){
            String opDelete="delete from Tarea where idTarea=?";

            try (Connection c=DriverManager.getConnection(URL);
            PreparedStatement ps=c.prepareStatement(opDelete)
            ){
                ps.setString(1,idTarea);
                ps.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    public void borrarEtiqueta(String nombreEtiqueta){

            String opDelete="delete from Etiqueta where nombreEtiqueta=?";

            try (Connection c=DriverManager.getConnection(URL);
            PreparedStatement ps=c.prepareStatement(opDelete)
            ){
                c.createStatement().execute("PRAGMA foreign_keys = ON;");
                ps.setString(1,nombreEtiqueta);
                ps.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public void guardarTarea (Tarea tarea){
        String opTareas = "INSERT OR REPLACE INTO Tarea (Titulo, FechaInicio, FechaFin, EstadoTarea, Hora, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c=DriverManager.getConnection(URL);
             PreparedStatement psTareas=c.prepareStatement(opTareas);
        ){
            psTareas.setString(1, tarea.getNombreTarea());
            psTareas.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
            psTareas.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
            psTareas.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null); // Mejor usar .name() para el Enum
            psTareas.setString(5, tarea.getHora() != null ? tarea.getHora().toString() : null);
            psTareas.setString(6, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);

            psTareas.setString(7, tarea.getDescripcion());
            psTareas.setString(8, tarea.getSitio());
            psTareas.setString(9, tarea.getIdTarea());
            psTareas.setString(10, tarea.getIdFamilia());
            psTareas.setString(11, tarea.getEtiqueta() != null ? tarea.getEtiqueta().getNombreEtiqueta() : null);
            psTareas.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void guardarEtiqueta(Etiqueta etiqueta){

        String opEtiquetas = "INSERT OR REPLACE INTO Etiqueta (codColor, nombreEtiqueta) VALUES (?,?)";

        try (Connection c=DriverManager.getConnection(URL);
             PreparedStatement psEtiquetas=c.prepareStatement(opEtiquetas);
        ){
            psEtiquetas.setString(1,etiqueta.getCodColor());
            psEtiquetas.setString(2,etiqueta.getNombreEtiqueta());
            psEtiquetas.executeUpdate();

    } catch (Exception e) {
            throw new RuntimeException(e);
        }

        }
    }