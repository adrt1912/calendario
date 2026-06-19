package Model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ConexionBD {

    private static ConexionBD conexionBD;
    private ConexionBD(){
        conexionBD=this;
    }

    public static ConexionBD getConexionBD() {
        if (conexionBD == null) {
            conexionBD = new ConexionBD();
        }
        return conexionBD;
    }

    public void crearTablasSiNoExisten() {
        // CAMBIO: Se sustituye 'Hora' por 'HoraInicio' y se añade 'HoraFin'
        String sqlTareas = "CREATE TABLE IF NOT EXISTS Tarea (" +
                "Titulo TEXT, FechaInicio TEXT, FechaFin TEXT, EstadoTarea TEXT, " +
                "HoraInicio TEXT, HoraFin TEXT, Frecuencia TEXT, Descripcion TEXT, Sitio TEXT, " +
                "idTarea TEXT PRIMARY KEY, idFamilia TEXT, Etiqueta TEXT);";

        String sqlEtiquetas = "CREATE TABLE IF NOT EXISTS Etiqueta (" +
                "codColor TEXT, nombreEtiqueta TEXT PRIMARY KEY);";

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps1 = c.prepareStatement(sqlTareas);
             PreparedStatement ps2 = c.prepareStatement(sqlEtiquetas)) {

            ps1.execute();
            ps2.execute();
        } catch (Exception e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }

    private static final String URL = "jdbc:sqlite:base_tareas.db";

    public void cargarDatosDeBD(){

        String op1="select * from Etiqueta";
        String op="select * from Tarea";

        try (Connection c= DriverManager.getConnection(URL);
             PreparedStatement ps=c.prepareStatement(op);
             PreparedStatement ps1=c.prepareStatement(op1);
        ){
            ResultSet rs1=ps1.executeQuery();
            while (rs1.next()){
                String nomE = rs1.getString("nombreEtiqueta");
                String color = rs1.getString("codColor");
                Etiqueta nuevaEtiqueta = new Etiqueta(nomE,color);
                GestorTareas.getGestorTareas().getListaEtiquetas().add(nuevaEtiqueta);
            }

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

                // CAMBIO: Leer HoraInicio y HoraFin
                String horaInicioStr = rs.getString("HoraInicio");
                LocalTime timeInicio = (horaInicioStr != null && !horaInicioStr.isBlank() && !horaInicioStr.equals("null"))
                        ? LocalTime.parse(horaInicioStr) : null;

                String horaFinStr = rs.getString("HoraFin");
                LocalTime timeFin = (horaFinStr != null && !horaFinStr.isBlank() && !horaFinStr.equals("null"))
                        ? LocalTime.parse(horaFinStr) : null;

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

                // CAMBIO: Se pasan timeInicio y timeFin al constructor de Tarea
                Tarea tarea = new Tarea(titulo, fechainic, fechaFin, estadoTarea, descripcion, sitio, timeInicio, timeFin, frecuencia, idFamilia, etiquetaAsignada);
                if (idTarea != null) {
                    tarea.setIdTarea(idTarea);
                }
                GestorTareas.getGestorTareas().añadirTareaALista(tarea);
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

    public void borrarEtiqueta(String nombreEtiqueta) {
        String opDelete = "DELETE FROM Etiqueta WHERE NombreEtiqueta=?";

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            c.createStatement().execute("PRAGMA foreign_keys = ON;");
            ps.setString(1, nombreEtiqueta);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void guardarTarea (Tarea tarea){
        // CAMBIO: Añadidos HoraInicio y HoraFin a la consulta SQL y ajustado el número de interrogantes (?) a 12
        String opTareas = "INSERT OR REPLACE INTO Tarea (Titulo, FechaInicio, FechaFin, EstadoTarea, HoraInicio, HoraFin, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c=DriverManager.getConnection(URL);
             PreparedStatement psTareas=c.prepareStatement(opTareas);
        ){
            psTareas.setString(1, tarea.getNombreTarea());
            psTareas.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
            psTareas.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
            psTareas.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null);

            // CAMBIO: Asignación de HoraInicio y HoraFin en las posiciones 5 y 6
            psTareas.setString(5, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
            psTareas.setString(6, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);

            // Se desplaza el resto de variables una posición
            psTareas.setString(7, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);
            psTareas.setString(8, tarea.getDescripcion());
            psTareas.setString(9, tarea.getSitio());
            psTareas.setString(10, tarea.getIdTarea());
            psTareas.setString(11, tarea.getIdFamilia());
            psTareas.setString(12, tarea.getEtiqueta() != null ? tarea.getEtiqueta().getNombreEtiqueta() : null);

            psTareas.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void guardarEtiqueta(Etiqueta etiqueta){
        String opEtiquetas = "INSERT OR REPLACE INTO Etiqueta (nombreEtiqueta,codColor) VALUES (?,?)";
        try (Connection c=DriverManager.getConnection(URL);
             PreparedStatement psEtiquetas=c.prepareStatement(opEtiquetas);
        ){
            psEtiquetas.setString(1,etiqueta.getNombreEtiqueta());
            psEtiquetas.setString(2,etiqueta.getCodColor());
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void vaciarBaseDeDatos(){
        String borrarTareas="Delete from Tarea";
        String borrarEtiqueta="Delete from Etiqueta";
        try(Connection c=DriverManager.getConnection(URL);
            PreparedStatement psTareas=c.prepareStatement(borrarTareas);
            PreparedStatement psEtiquetas=c.prepareStatement(borrarEtiqueta);
        ) {
            psTareas.executeUpdate();
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}