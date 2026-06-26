package model;

import utils.SeguridadUtils;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConexionBD {

    // Patrón Singleton
    private static ConexionBD conexionBD;

    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);

    // La URL de la conexión estándar (estable de toda la vida)
    private static final String URL = "jdbc:sqlite:base_tareas.db";

    private ConexionBD() {}

    public static ConexionBD getConexionBD() {
        if (conexionBD == null) {
            conexionBD = new ConexionBD();
        }
        return conexionBD;
    }

    // MEtodo de conexión limpio y estándar
    private Connection obtenerConexion() throws SQLException {
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
            logger.error("Error al crear las tablas en la base de datos: ", e);
        }
    }

    public void cargarDatosDeBD(int idObtenido) {
        GestorTareas.getGestorTareas().getListaEtiquetas().clear();
        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion()) {
            cargarEtiquetasDesdeBD(c, idObtenido, clave);
            cargarTareasDesdeBD(c, idObtenido, clave);
        } catch (Exception e) {
            logger.error("Error crítico al procesar la carga general de datos cifrados: ", e);
        }
    }

    private void cargarEtiquetasDesdeBD(Connection c, int idObtenido, javax.crypto.spec.SecretKeySpec clave) throws SQLException {
        String sqlEtiquetas = "SELECT id, nombreEtiqueta, codColor FROM Etiqueta WHERE usuario_id = ?";
        try (PreparedStatement ps1 = c.prepareStatement(sqlEtiquetas)) {
            ps1.setInt(1, idObtenido);
            try (ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    String nomE = SeguridadUtils.descifrarTexto(rs1.getString("nombreEtiqueta"), clave);
                    String color = rs1.getString("codColor");
                    Etiqueta nuevaEtiqueta = new Etiqueta(nomE, color);
                    GestorTareas.getGestorTareas().getListaEtiquetas().add(nuevaEtiqueta);
                }
            }
        }
    }

    private void cargarTareasDesdeBD(Connection c, int idObtenido, javax.crypto.spec.SecretKeySpec clave) throws SQLException {
        String sqlTareas = "SELECT Titulo, FechaInicio, FechaFin, EstadoTarea, HoraInicio, HoraFin, Frecuencia, Descripcion, Sitio, idTarea, idFamilia, Etiqueta FROM Tarea WHERE usuario_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sqlTareas)) {
            ps.setInt(1, idObtenido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Procesamos cada fila de forma independiente
                    procesarFilaTarea(rs, clave);
                }
            }
        }
    }

    //  submetodo aislado para procesar la fila individualmente (Mantiene la claridad)
    private void procesarFilaTarea(ResultSet rs, javax.crypto.spec.SecretKeySpec clave) throws SQLException {
        String titulo = SeguridadUtils.descifrarTexto(rs.getString("Titulo"), clave);
        String descripcion = SeguridadUtils.descifrarTexto(rs.getString("Descripcion"), clave);
        String sitio = SeguridadUtils.descifrarTexto(rs.getString("Sitio"), clave);
        String etiqueta = SeguridadUtils.descifrarTexto(rs.getString("Etiqueta"), clave);

        // Parseos limpios usando los ayudantes de abajo
        LocalDate fechainic = parsearFecha(rs.getString("FechaInicio"));
        LocalDate fechaFin = parsearFecha(rs.getString("FechaFin"));

        String estadoTareaS = rs.getString("EstadoTarea");
        EstadoTarea estadoTarea = esCadenaValida(estadoTareaS) ? EstadoTarea.valueOf(estadoTareaS) : null;

        LocalTime timeInicio = parsearHora(rs.getString("HoraInicio"));
        LocalTime timeFin = parsearHora(rs.getString("HoraFin"));

        String frecuenciaStr = rs.getString("Frecuencia");
        Periodicidad frecuencia = esCadenaValida(frecuenciaStr) ? Periodicidad.valueOf(frecuenciaStr) : null;

        String idTarea = rs.getString("idTarea");
        String idFamilia = rs.getString("idFamilia");

        Etiqueta etiquetaAsignada = buscarEtiquetaEnLista(etiqueta);

        // Construcción limpia con el record
        TareaDatos datos = new TareaDatos(titulo, fechainic, fechaFin, descripcion, sitio, timeInicio, timeFin, frecuencia, idFamilia, etiquetaAsignada);
        Tarea tarea = new Tarea(datos, estadoTarea);

        if (idTarea != null) {
            tarea.setIdTarea(idTarea);
        }

        GestorTareas.getGestorTareas().aniadirTareaAListaDeDocumento(tarea);
    }

    private boolean esCadenaValida(String str) {
        return str != null && !str.isBlank() && !"null".equals(str);
    }

    private LocalDate parsearFecha(String fechaStr) {
        return esCadenaValida(fechaStr) ? LocalDate.parse(fechaStr) : null;
    }

    private LocalTime parsearHora(String horaStr) {
        return esCadenaValida(horaStr) ? LocalTime.parse(horaStr) : null;
    }

    private Etiqueta buscarEtiquetaEnLista(String nombreEtiqueta) {
        if (nombreEtiqueta == null) {
            return null;
        }
        return GestorTareas.getGestorTareas().getListaEtiquetas().stream()
                .filter(e -> e.nombreEtiqueta() != null && e.nombreEtiqueta().equals(nombreEtiqueta))
                .findFirst()
                .orElse(null);
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
            throw new IllegalStateException("Error al eliminar la tarea con ID: " + idTarea, e);
        }
    }

    // Borra una etiqueta de la BD
    public void borrarEtiqueta(String nombreEtiqueta) {
        int idActivo = GestorTareas.getGestorTareas().getIdUsuarioLogueado();
        String opDelete = "DELETE FROM Etiqueta WHERE nombreEtiqueta=? and usuario_id=?"; // Columna corregida

        var clave = GestorTareas.getGestorTareas().getClaveCifradoActiva();

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(opDelete)
        ) {
            ps.setString(1, SeguridadUtils.cifrarTexto(nombreEtiqueta, clave));
            ps.setInt(2, idActivo);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Error al intentar eliminar la etiqueta: " + nombreEtiqueta, e);
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
            throw new IllegalStateException("Error al purgar el perfil de usuario con ID: " + idUsusario, e);
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
            throw new IllegalStateException("Error crítico al persistir la tarea cifrada en la base de datos", e);
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
            psEtiquetas.setString(1, SeguridadUtils.cifrarTexto(etiqueta.nombreEtiqueta(), clave));
            psEtiquetas.setString(2, etiqueta.codColor());
            psEtiquetas.setInt(3, idActivo);
            psEtiquetas.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Error crítico al registrar la nueva etiqueta cifrada", e);
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
            throw new IllegalStateException("Error al vaciar los registros del panel del usuario", e);
        }
    }

    // Autentica al usuario y activa su clave criptográfica simétrica
    public int verificarUsuarioYObtenerId(String nombreUsuario, String pinIntroducido) {
        String sql = "SELECT id, pin_hash FROM Usuario WHERE nombre_usuario = ?";

        try (Connection c = obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String hashGuardado = rs.getString("pin_hash");

                    if (GestorTareas.getGestorTareas().verificarHash(pinIntroducido, hashGuardado)) {
                        GestorTareas.getGestorTareas().setClaveCifradoActiva(SeguridadUtils.generarClaveDesdePIN(pinIntroducido));
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error al autenticar usuario en el sistema: ", e);
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
                GestorTareas.getGestorTareas().setClaveCifradoActiva(SeguridadUtils.generarClaveDesdePIN(pinPlano));
            }
            return insertado;
        } catch (Exception e) {
            logger.error("Error al registrar usuario en la BD: ", e);
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String idiomaDB = rs.getString("idioma");
                    boolean modoOscuro = rs.getInt("modo_oscuro") == 1;

                    if (idiomaDB == null || idiomaDB.isBlank()) idiomaDB = "es";

                    Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
                    prefs.put("idioma_actual", idiomaDB);
                    prefs.putBoolean("modo_oscuro", modoOscuro);

                    java.util.Locale nuevoLocale = new java.util.Locale(idiomaDB);
                    java.util.Locale.setDefault(nuevoLocale);

                    for (Idiomas idm : Idiomas.values()) {
                        if (idm.getCodigo().equalsIgnoreCase(idiomaDB)) {
                            GestorTareas.getGestorTareas().setIdioma(idm);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error al cargar las preferencias del perfil de usuario", e);
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
            throw new IllegalStateException("Error al guardar las preferencias de interfaz de usuario", e);
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
                // Purgamos y re-encriptamos las etiquetas del usuario
                psEtiquetas.setInt(1, idUsuarioCambiar);
                psEtiquetas.executeUpdate();
                reencriptarEtiquetasBatch(psInsertEtiqueta, idUsuarioCambiar, claveNueva);

                // Modificamos todas las tareas aplicando el nuevo escudo AES
                reencriptarTareasBatch(psTareas, idUsuarioCambiar, claveNueva);

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
            logger.error("Error crítico al re-cifrar los registros por cambio de PIN: ", e);
            return false;
        }
    }

    //  SUBMeTODO 1: Aísla el procesamiento por lotes de las etiquetas
    private void reencriptarEtiquetasBatch(PreparedStatement psInsert, int idUsuario, javax.crypto.spec.SecretKeySpec claveNueva) throws SQLException {
        psInsert.setInt(3, idUsuario);
        for (Etiqueta etiqueta : GestorTareas.getGestorTareas().getListaEtiquetas()) {
            if (!"Sin Etiqueta".equals(etiqueta.nombreEtiqueta())) {
                psInsert.setString(1, SeguridadUtils.cifrarTexto(etiqueta.nombreEtiqueta(), claveNueva));
                psInsert.setString(2, etiqueta.codColor());
                psInsert.addBatch();
            }
        }
        psInsert.executeBatch();
    }

    // SUBMeTODO 2: Controla de forma limpia el bucle de actualización masiva de tareas
    private void reencriptarTareasBatch(PreparedStatement psTareas, int idUsuario, javax.crypto.spec.SecretKeySpec claveNueva) throws SQLException {
        psTareas.setInt(12, idUsuario);
        for (Tarea tarea : GestorTareas.getGestorTareas().getTodasTareas()) {
            mapearParametrosTareaBatch(psTareas, tarea, claveNueva);
            psTareas.addBatch();
        }
        psTareas.executeBatch();
    }

    //  SUBMeTODO 3: Absorbe la batería de operadores ternarios para que no penalicen el flujo principal
    private void mapearParametrosTareaBatch(PreparedStatement ps, Tarea tarea, javax.crypto.spec.SecretKeySpec claveNueva) throws SQLException {
        ps.setString(1, SeguridadUtils.cifrarTexto(tarea.getNombreTarea(), claveNueva));
        ps.setString(2, tarea.getFechaInicio() != null ? tarea.getFechaInicio().toString() : null);
        ps.setString(3, tarea.getFechaFin() != null ? tarea.getFechaFin().toString() : null);
        ps.setString(4, tarea.getEstadoTarea() != null ? tarea.getEstadoTarea().name() : null);
        ps.setString(5, tarea.getHoraInicio() != null ? tarea.getHoraInicio().toString() : null);
        ps.setString(6, tarea.getHoraFin() != null ? tarea.getHoraFin().toString() : null);
        ps.setString(7, tarea.getFrecuencia() != null ? tarea.getFrecuencia().name() : null);
        ps.setString(8, SeguridadUtils.cifrarTexto(tarea.getDescripcion(), claveNueva));
        ps.setString(9, SeguridadUtils.cifrarTexto(tarea.getSitio(), claveNueva));
        ps.setString(10, tarea.getIdFamilia());
        ps.setString(11, tarea.getEtiqueta() != null ? SeguridadUtils.cifrarTexto(tarea.getEtiqueta().nombreEtiqueta(), claveNueva) : null);
        ps.setString(13, tarea.getIdTarea()); // Filtro WHERE
    }
}