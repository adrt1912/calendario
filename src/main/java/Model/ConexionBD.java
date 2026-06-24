package Model;

import Utils.SeguridadUtils;
import javafx.scene.Scene;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.prefs.Preferences;

public class ConexionBD {

    //Es Singletones
    private static ConexionBD conexionBD;

    private ConexionBD() {
        conexionBD = this;
    }

    public static ConexionBD getConexionBD() {
        if (conexionBD == null) conexionBD = new ConexionBD();
        return conexionBD;
    }

    //En caso de que no existan se crean
    public void crearTablasSiNoExisten() {
        // CAMBIO: Se sustituye 'Hora' por 'HoraInicio' y se añade 'HoraFin'
        String sqlTareas = "CREATE TABLE IF NOT EXISTS Tarea (" +
                "Titulo TEXT, FechaInicio TEXT, FechaFin TEXT, EstadoTarea TEXT, " +
                "HoraInicio TEXT, HoraFin TEXT, Frecuencia TEXT, Descripcion TEXT, Sitio TEXT, " +
                "idTarea TEXT PRIMARY KEY, idFamilia TEXT, Etiqueta TEXT, " +
                "usuario_id INTEGER NOT NULL, " +
                "FOREIGN KEY (usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE);";

        String sqlEtiquetas = "CREATE TABLE IF NOT EXISTS Etiqueta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "codColor TEXT NOT NULL, " +
                "nombreEtiqueta TEXT NOT NULL, " +
                "usuario_id INTEGER NOT NULL, " +
                "FOREIGN KEY (usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE);";

        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS Usuario(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT , nombre_usuario TEXT NOT NULL UNIQUE," +
                " pin_hash TEXT not null , idioma TEXT default'es', modo_oscuro INTEGER default 0)";

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps1 = c.prepareStatement(sqlTareas);
             PreparedStatement ps2 = c.prepareStatement(sqlEtiquetas);
             PreparedStatement ps3 = c.prepareStatement(sqlUsuarios)) {
            ps1.execute();
            ps2.execute();
            ps3.execute();
        } catch (Exception e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }

    //La url de la conexion
    private static final String URL = "jdbc:sqlite:base_tareas.db";

    //Lee toda la BD y los guarda en local
    public void cargarDatosDeBD(int idObtenido) {

        String op1 = "select * from Etiqueta where usuario_id=?";
        String op = "select * from Tarea where usuario_id=?";
        GestorTareas.getGestorTareas().getListaEtiquetas().clear();

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(op);
             PreparedStatement ps1 = c.prepareStatement(op1)
        ) {
            //Leemos primero las etiquetas
            ps1.setInt(1, idObtenido);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                String nomE = rs1.getString("nombreEtiqueta");
                String color = rs1.getString("codColor");
                Etiqueta nuevaEtiqueta = new Etiqueta(nomE, color);
                GestorTareas.getGestorTareas().getListaEtiquetas().add(nuevaEtiqueta);
            }

            //Leemos las tareas
            ps.setInt(1, idObtenido);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
                            .filter(e -> e.nombreEtiqueta() != null && e.nombreEtiqueta().equals(etiqueta))
                            .findFirst()
                            .orElse(null);
                }

                Tarea tarea = new Tarea(titulo, fechainic, fechaFin, estadoTarea, descripcion, sitio, timeInicio, timeFin, frecuencia, idFamilia, etiquetaAsignada);
                if (idTarea != null) tarea.setIdTarea(idTarea);

                GestorTareas.getGestorTareas().aniadirTareaAListaDeDocumento(tarea);
            }
        } catch (Exception ignored) {
        }
    }

    //Borra una tarea de la BD
    public void borrarTarea(String idTarea) {
        String opDelete = "delete from Tarea where idTarea=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            ps.setString(1, idTarea);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Borra una etiqueta de la BD
    public void borrarEtiqueta(String nombreEtiqueta) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opDelete = "DELETE FROM Etiqueta WHERE NombreEtiqueta=? and usuario_id=?";

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            c.createStatement().execute("PRAGMA foreign_keys = ON;");
            ps.setString(1, nombreEtiqueta);
            ps.setInt(2, idActivo);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void borrarPerfil(int idUsusario){
        String sql = "DELETE FROM Usuario WHERE id = ?";
        try (Connection c=DriverManager.getConnection(URL);
        PreparedStatement ps=c.prepareStatement(sql)){
            c.createStatement().execute("PRAGMA foreign_keys = ON;");

            ps.setInt(1, idUsusario);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Guarda una tarea nueva
    public void guardarTarea(Tarea tarea) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        // CAMBIO: Añadidos HoraInicio y HoraFin a la consulta SQL y ajustado el número de interrogantes (?) a 12
        String opTareas = "INSERT OR REPLACE INTO Tarea (Titulo, FechaInicio, FechaFin, EstadoTarea, HoraInicio, HoraFin, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta,usuario_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement psTareas = c.prepareStatement(opTareas)
        ) {
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
            psTareas.setString(12, tarea.getEtiqueta() != null ? tarea.getEtiqueta().nombreEtiqueta() : null);
            psTareas.setInt(13, idActivo);

            psTareas.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Gurada una etiqueta nueva
    public void guardarEtiqueta(Etiqueta etiqueta) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opEtiquetas = "INSERT OR REPLACE INTO Etiqueta (nombreEtiqueta,codColor,usuario_id) VALUES (?,?,?)";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement psEtiquetas = c.prepareStatement(opEtiquetas)
        ) {
            psEtiquetas.setString(1, etiqueta.nombreEtiqueta());
            psEtiquetas.setString(2, etiqueta.codColor());
            psEtiquetas.setInt(3, idActivo);
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Para borrar todos los datos de la BD
    public void vaciarBaseDeDatos() {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String borrarTareas = "Delete from Tarea where usuario_id=?";
        String borrarEtiqueta = "Delete from Etiqueta where  usuario_id=?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement psTareas = c.prepareStatement(borrarTareas);
             PreparedStatement psEtiquetas = c.prepareStatement(borrarEtiqueta)
        ) {
            psTareas.setInt(1, idActivo);
            psEtiquetas.setInt(1, idActivo);
            psTareas.executeUpdate();
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int verificarUsuarioYObtenerId(String nombreUsuario, String pinIntroducido) {
        String sql = "SELECT id, pin_hash FROM Usuario WHERE nombre_usuario = ?";

        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String hashGuardado = rs.getString("pin_hash");

                // Aquí debes usar tu metodo de encriptación habitual (ej. SHA-256)
                // Si el PIN tecleado (encriptado) coincide con el de la BD... ¡bingo!
                if (GestorTareas.getGestorTareas().verificarHash(pinIntroducido, hashGuardado)) {
                    return id; // Devolvemos el ID del usuario
                }
            }
        } catch (Exception e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }
        return -1;
    }

    public boolean registrarNuevoUsuario(String nombreUsuario,String pinPlano){
        String sql="insert into Usuario (nombre_usuario,pin_hash) values(?,?)";
        String hash= SeguridadUtils.encriptarPIN(pinPlano);

        try (Connection c=DriverManager.getConnection(URL);
        PreparedStatement ps =c.prepareStatement(sql)){

            c.createStatement().execute("PRAGMA foreign_keys = ON;");
            ps.setString(1,nombreUsuario);
            ps.setString(2,hash);
            return  ps.executeUpdate() >0;
        } catch (Exception e) {
            System.err.println("Error al registrar usuario en la BD: " + e.getMessage());
            return false;
        }
    }

    public void cargarDatosUsuario( int idUsuario){

        String sql="select idioma,modo_oscuro from Usuario where id=?";

        try (Connection c=DriverManager.getConnection(URL);
        PreparedStatement ps=c.prepareStatement(sql);
        ){
            ps.setInt(1,idUsuario);
            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                String idiomaDB = rs.getString("idioma");
                boolean modoOscuro = rs.getInt("modo_oscuro") == 1;

                if (idiomaDB == null || idiomaDB.isBlank()) idiomaDB = "es";

                Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
                prefs.put("idioma_actual", idiomaDB);
                prefs.putBoolean("modo_oscuro", modoOscuro);


                java.util.Locale nuevoLocale = new java.util.Locale(idiomaDB);
                java.util.Locale.setDefault(nuevoLocale);

                // 3. Buscamos el objeto de tu Enum "Idiomas" que coincida con el código de la BD y lo asignamos
                for (Idiomas idm : Idiomas.values()) {
                    if (idm.getCodigo().equalsIgnoreCase(idiomaDB)) {
                        GestorTareas.getGestorTareas().setIdioma(idm);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void guardarDatosUsuario(int idUsuario, String nuevoIdioma, boolean modoOscuro){

        String sql1 = "UPDATE Usuario SET idioma = ?, modo_oscuro = ? WHERE id = ?";
        int modoOscuroInt = modoOscuro ? 1 : 0;

        try (Connection c=DriverManager.getConnection(URL);
        PreparedStatement ps1=c.prepareStatement(sql1)){

            ps1.setString(1,nuevoIdioma);
            ps1.setInt(2,modoOscuroInt);
            ps1.setInt(3,idUsuario);

            ps1.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}