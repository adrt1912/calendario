package Model;

import Utils.SeguridadUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.prefs.Preferences;

public class ConexionBD {

    // Patrón Singleton
    private static ConexionBD conexionBD;

    // La URL de la conexión estándar (estable de toda la vida)
    private static final String URL = "jdbc:sqlite:base_tareas.db";

    private ConexionBD() {
        conexionBD = this;
    }

    public static ConexionBD getConexionBD() {
        if (conexionBD == null) conexionBD = new ConexionBD();
        return conexionBD;
    }

    // Método de conexión limpio y estándar
    private Connection obtenerConexion() throws Exception {
        Connection c = DriverManager.getConnection(URL);
        try (Statement stmt = c.createStatement()) {
            // Activamos las claves foráneas obligatorias
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return c;
    }

    // Crea las tablas si no existen de forma normal
    public void crearTablasSiNoExisten() {
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

        try (Connection c = obtenerConexion();
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

    // Lee la BD y descifra los textos usando la clave única del usuario activo
    public void cargarDatosDeBD(int idObtenido) {
        String op1 = "select * from Etiqueta where usuario_id=?";
        String op = "select * from Tarea where usuario_id=?";
        GestorTareas.getGestorTareas().getListaEtiquetas().clear();

        // 🔑 Recuperamos la clave AES exclusiva de la sesión actual
        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(op);
             PreparedStatement ps1 = c.prepareStatement(op1)
        ) {
            // Leemos primero las etiquetas
            ps1.setInt(1, idObtenido);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                // 🔓 DESCIFRAMOS el nombre de la etiqueta usando la clave de sesión
                String nomE = SeguridadUtils.descifrarTexto(rs1.getString("nombreEtiqueta"), clave);
                String color = rs1.getString("codColor");
                Etiqueta nuevaEtiqueta = new Etiqueta(nomE, color);
                GestorTareas.getGestorTareas().getListaEtiquetas().add(nuevaEtiqueta);
            }

            // Leemos las tareas
            ps.setInt(1, idObtenido);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // 🔓 DESCIFRAMOS los datos usando la clave de sesión
                String titulo = SeguridadUtils.descifrarTexto(rs.getString("Titulo"), clave);
                String descripcion = SeguridadUtils.descifrarTexto(rs.getString("Descripcion"), clave);
                String sitio = SeguridadUtils.descifrarTexto(rs.getString("Sitio"), clave);
                String etiqueta = SeguridadUtils.descifrarTexto(rs.getString("Etiqueta"), clave);

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

                String horaInicioStr = rs.getString("HoraInicio");
                LocalTime timeInicio = (horaInicioStr != null && !horaInicioStr.isBlank() && !horaInicioStr.equals("null"))
                        ? LocalTime.parse(horaInicioStr) : null;

                String horaFinStr = rs.getString("HoraFin");
                LocalTime timeFin = (horaFinStr != null && !horaFinStr.isBlank() && !horaFinStr.equals("null"))
                        ? LocalTime.parse(horaFinStr) : null;

                String frecuenciaStr = rs.getString("Frecuencia");
                Periodicidad frecuencia = (frecuenciaStr != null && !frecuenciaStr.isBlank() && !frecuenciaStr.equals("null"))
                        ? Periodicidad.valueOf(frecuenciaStr) : null;

                String idTarea = rs.getString("idTarea");
                String idFamilia = rs.getString("idFamilia");

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

    // Borra una tarea de la BD
    public void borrarTarea(String idTarea) {
        String opDelete = "delete from Tarea where idTarea=?";
        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            ps.setString(1, idTarea);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Borra una etiqueta de la BD (Cifrando el nombre con la clave del usuario para hacer Match)
    public void borrarEtiqueta(String nombreEtiqueta) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opDelete = "DELETE FROM Etiqueta WHERE NombreEtiqueta=? and usuario_id=?";

        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            // 🔒 Pasamos el nombre cifrado con su clave específica
            ps.setString(1, SeguridadUtils.cifrarTexto(nombreEtiqueta, clave));
            ps.setInt(2, idActivo);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Borra por completo el perfil del usuario activo
    public void borrarPerfil(int idUsusario){
        String sql = "DELETE FROM Usuario WHERE id = ?";
        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, idUsusario);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Cifra los campos usando la contraseña de sesión del usuario logueado antes de guardar
    public void guardarTarea(Tarea tarea) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opTareas = "INSERT OR REPLACE INTO Tarea (Titulo, FechaInicio, FechaFin, EstadoTarea, HoraInicio, HoraFin, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta,usuario_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion();
             PreparedStatement psTareas = c.prepareStatement(opTareas)
        ) {
            // 🔒 APLICAMOS CIFRADO AES BASADO EN EL USUARIO ACTIVO
            psTareas.setString(1, SeguridadUtils.cifrarTexto(tarea.getNombreTarea(), clave));
            psTareas.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
            psTareas.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
            psTareas.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null);
            psTareas.setString(5, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
            psTareas.setString(6, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);
            psTareas.setString(7, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);
            psTareas.setString(8, SeguridadUtils.cifrarTexto(tarea.getDescripcion(), clave));
            psTareas.setString(9, SeguridadUtils.cifrarTexto(tarea.getSitio(), clave));
            psTareas.setString(10, tarea.getIdTarea());
            psTareas.setString(11, tarea.getIdFamilia());
            psTareas.setString(12, tarea.getEtiqueta() != null ? SeguridadUtils.cifrarTexto(tarea.getEtiqueta().nombreEtiqueta(), clave) : null);
            psTareas.setInt(13, idActivo);

            psTareas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Guarda una etiqueta nueva cifrando su nombre descriptivo
    public void guardarEtiqueta(Etiqueta etiqueta) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opEtiquetas = "INSERT OR REPLACE INTO Etiqueta (nombreEtiqueta,codColor,usuario_id) VALUES (?,?,?)";

        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion();
             PreparedStatement psEtiquetas = c.prepareStatement(opEtiquetas)
        ) {
            // 🔒 Ciframos con la clave exclusiva del usuario
            psEtiquetas.setString(1, SeguridadUtils.cifrarTexto(etiqueta.nombreEtiqueta(), clave));
            psEtiquetas.setString(2, etiqueta.codColor());
            psEtiquetas.setInt(3, idActivo);
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Vacía las tareas y etiquetas del usuario actual de la BD
    public void vaciarBaseDeDatos() {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String borrarTareas = "Delete from Tarea where usuario_id=?";
        String borrarEtiqueta = "Delete from Etiqueta where usuario_id=?";
        try (Connection c = obtenerConexion();
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

    // Autentica al usuario y activa su clave criptográfica simétrica
    public int verificarUsuarioYObtenerId(String nombreUsuario, String pinIntroducido) {
        String sql = "SELECT id, pin_hash FROM Usuario WHERE nombre_usuario = ?";

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String hashGuardado = rs.getString("pin_hash");

                if (GestorTareas.getGestorTareas().verificarHash(pinIntroducido, hashGuardado)) {
                    // 🚀 PERFECTO: Guardamos la clave criptográfica derivada de este PIN en el Gestor
                    GestorTareas.getGestorTareas().setClaveCifradoActiva(SeguridadUtils.generarClaveDesdePIN(pinIntroducido));
                    return id;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }
        return -1;
    }

    // Registra un nuevo perfil de usuario
    public boolean registrarNuevoUsuario(String nombreUsuario, String pinPlano){
        String sql = "insert OR IGNORE into Usuario (nombre_usuario,pin_hash) values(?,?)";
        String hash = SeguridadUtils.encriptarPIN(pinPlano);

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, nombreUsuario);
            ps.setString(2, hash);

            boolean insertado = ps.executeUpdate() > 0;
            if (insertado) {
                // 🚀 Al registrarse con éxito, también dejamos su sesión criptográfica preconfigurada
                GestorTareas.getGestorTareas().setClaveCifradoActiva(SeguridadUtils.generarClaveDesdePIN(pinPlano));
            }
            return insertado;
        } catch (Exception e) {
            System.err.println("Error al registrar usuario en la BD: " + e.getMessage());
            return false;
        }
    }

    // Recupera la configuración de idioma y tema visual del usuario
    public void cargarDatosUsuario(int idUsuario){
        String sql = "select idioma,modo_oscuro from Usuario where id=?";

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)
        ){
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                String idiomaDB = rs.getString("idioma");
                boolean modoOscuro = rs.getInt("modo_oscuro") == 1;

                if (idiomaDB == null || idiomaDB.isBlank()) idiomaDB = "es";

                Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
                prefs.put("idioma_actual", idiomaDB);
                prefs.putBoolean("modo_oscuro", modoOscuro);

                java.util.Locale nuevoLocale = new java.util.Locale(idiomaDB);
                java.util.Locale.setDefault(nuevoLocale);

                // El metodo verificarUsuarioYObtenerId ya generó la clave dinámicamente antes de llegar aquí,
                // así que hemos eliminado la línea rota que llamaba al metodo sin parámetros.

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

    // Actualiza los ajustes del usuario en la base de datos
    public void guardarDatosUsuario(int idUsuario, String nuevoIdioma, boolean modoOscuro){
        String sql1 = "UPDATE Usuario SET idioma = ?, modo_oscuro = ? WHERE id = ?";
        int modoOscuroInt = modoOscuro ? 1 : 0;

        try (Connection c = obtenerConexion();
             PreparedStatement ps1 = c.prepareStatement(sql1)){
            ps1.setString(1, nuevoIdioma);
            ps1.setInt(2, modoOscuroInt);
            ps1.setInt(3, idUsuario);

            ps1.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cambiarPin(String nuevoPin){

        var claveVieja = GestorTareas.getGestorTareas().getClaveCifradoActiva();
        int idUsuarioCambiar = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        if (idUsuarioCambiar == -1) return false;

        String nuevaClaveHash = SeguridadUtils.encriptarPIN(nuevoPin);

        String op1 = "UPDATE Usuario set pin_hash=? where id=?";
        String sqlUpdateTarea = "UPDATE Tarea SET Titulo = ?, FechaInicio = ?, FechaFin = ?, EstadoTarea = ?, HoraInicio = ?, HoraFin = ?, Frecuencia = ?, Descripcion = ?, Sitio = ?, idFamilia = ?, Etiqueta = ?, usuario_id = ? WHERE idTarea = ?";
        String sqlBorrarEtiquetas = "DELETE FROM Etiqueta WHERE usuario_id = ?";
        String sqlInsertEtiqueta = "INSERT OR REPLACE INTO Etiqueta (nombreEtiqueta, codColor, usuario_id) VALUES (?,?,?)";

        try (Connection c = obtenerConexion();
             PreparedStatement ps1 = c.prepareStatement(op1);
             PreparedStatement psTareas = c.prepareStatement(sqlUpdateTarea);
             PreparedStatement psEtiquetas = c.prepareStatement(sqlBorrarEtiquetas);
             PreparedStatement psInsertEtiqueta = c.prepareStatement(sqlInsertEtiqueta)
        ){
            javax.crypto.spec.SecretKeySpec claveNueva = SeguridadUtils.generarClaveDesdePIN(nuevoPin);
            c.setAutoCommit(false); // Iniciamos transacción manual

            ps1.setString(1, nuevaClaveHash);
            ps1.setInt(2, idUsuarioCambiar);

            int numa = ps1.executeUpdate();
            if(numa == 1){
                //  Purgamos del disco las etiquetas encriptadas con el PIN viejo
                psEtiquetas.setInt(1, idUsuarioCambiar);
                psEtiquetas.executeUpdate();

                //  Insertamos las etiquetas en lote con el nuevo cifrado
                for (Etiqueta etiqueta : GestorTareas.getGestorTareas().getListaEtiquetas()) {
                    if (!etiqueta.nombreEtiqueta().equals("Sin Etiqueta")) {

                        psInsertEtiqueta.setString(1, SeguridadUtils.cifrarTexto(etiqueta.nombreEtiqueta(), claveNueva));
                        psInsertEtiqueta.setString(2, etiqueta.codColor());
                        psInsertEtiqueta.setInt(3, idUsuarioCambiar);
                        psInsertEtiqueta.addBatch();
                    }
                }
                psInsertEtiqueta.executeBatch();

                // Modificamos todas las tareas del usuario aplicando el nuevo escudo AES
                for (Tarea tarea : GestorTareas.getGestorTareas().getTodasTareas()) {
                    psTareas.setString(1, SeguridadUtils.cifrarTexto(tarea.getNombreTarea(), claveNueva));
                    psTareas.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
                    psTareas.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
                    psTareas.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null);
                    psTareas.setString(5, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
                    psTareas.setString(6, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);
                    psTareas.setString(7, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);
                    psTareas.setString(8, SeguridadUtils.cifrarTexto(tarea.getDescripcion(), claveNueva));
                    psTareas.setString(9, SeguridadUtils.cifrarTexto(tarea.getSitio(), claveNueva));
                    psTareas.setString(10, tarea.getIdFamilia());
                    psTareas.setString(11, tarea.getEtiqueta() != null ? SeguridadUtils.cifrarTexto(tarea.getEtiqueta().nombreEtiqueta(), claveNueva) : null);
                    psTareas.setInt(12, idUsuarioCambiar);
                    psTareas.setString(13, tarea.getIdTarea()); // Filtro WHERE

                    psTareas.addBatch();
                }
                psTareas.executeBatch();

                //  Actualizamos la clave en memoria RAM y consolidamos los cambios en el archivo .db
                GestorTareas.getGestorTareas().setClaveCifradoActiva(claveNueva);
                c.commit();
                return true;
            } else {
                c.rollback();
                return false;
            }

        } catch (Exception e) {
            GestorTareas.getGestorTareas().setClaveCifradoActiva(claveVieja);
            System.err.println("Error crítico al re-cifrar por cambio de PIN: " + e.getMessage());
            return false;
        }
    }
}