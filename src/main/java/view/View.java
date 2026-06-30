package view;

import model.GestorTareas;
import model.Tarea;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class View {

    private static Stage stage;

    private View(){}

    // Metodo para fijar el stage que viene de la Application
    public static void setPrimaryStage(Stage s) {
        stage = s;
    }

    public static Stage getPrimaryStage() {
        return stage;
    }

    private static ResourceBundle obtenerBundleActual() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
            String codIdioma = prefs.get("idioma_actual", "es");
            Locale locale = new Locale(codIdioma);
            return ResourceBundle.getBundle("textos", locale);
        } catch (Exception e) {
            return ResourceBundle.getBundle("textos", new Locale("es"));
        }
    }
    // Ventana inicial (Menú Principal)
    public static void showInitialView() throws IOException {
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        String lang = prefs.get("idioma_actual", "es");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        if (stage == null) stage = new Stage();

        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/MenuPrincipal.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 1800, 900);

        if (prefs.getBoolean("modo_oscuro", false)) scene.getRoot().getStyleClass().add("dark-mode");

        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setTitle("Calendario");
        stage.setScene(scene);
        stage.show();
    }



    // PANTALLA B: La pantalla de bloqueo que salta al arrancar la app si ya existe un PIN
    public static void showPINInsert() throws IOException {
        ResourceBundle bundle = obtenerBundleActual();

        // CRITICAL: Asegúrate de que el archivo se llame EXACTAMENTE "menuPIN.fxml" (revisa las mayúsculas)
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/menuInsertarPin.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        if (stage == null) stage = new Stage();


        stage.setTitle("Introducir PIN de Acceso");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void showCrearPIN() throws IOException {
        ResourceBundle bundle = obtenerBundleActual();

        // CRITICAL: Asegúrate de que el archivo se llame EXACTAMENTE "menuPIN.fxml" (revisa las mayúsculas)
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/menuCrearPin.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        Stage ventanaModal = new Stage();

        ventanaModal.setTitle("Introducir PIN de Acceso");
        ventanaModal.setScene(scene);
        ventanaModal.setResizable(false);
        ventanaModal.showAndWait();
    }


    public static void showCrearTArea(LocalDate fecha, LocalTime hora) throws IOException {
        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/crearTarea.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(),800,800);

        controller.CrearTareaController controlador = fxmlLoader.getController();
        controlador.initialize(fecha, hora);

        Stage stage1 = new Stage();
        stage1.setTitle("Crear Tarea Nueva");
        stage1.sizeToScene();
        stage1.setResizable(false);
        stage1.setScene(scene);
        stage1.showAndWait();
    }

    public static void showTareaVentana(Tarea tarea) throws IOException {
        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/modificarTarea.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        controller.ModificarTareaController controlador = fxmlLoader.getController();
        controlador.setTareaMos(tarea);

        Stage stage1 = new Stage();
        stage1.setTitle("Modificar Tarea");
        stage1.sizeToScene();
        stage1.setResizable(false);
        stage1.setScene(scene);
        stage1.showAndWait();
    }

    public static void showConfirmacionEl(Tarea tarea) throws IOException {
        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/menuConfirmacionPer.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 222);

        controller.MenuConfirmacionBorrarController controlador = fxmlLoader.getController();
        controlador.setTareaMos(tarea);

        Stage stage1 = new Stage();
        stage1.setTitle("Eliminar Tarea");
        stage1.sizeToScene();
        stage1.setResizable(false);
        stage1.setScene(scene);
        stage1.showAndWait();
    }

    public static void showNuevaEtiqueta() throws IOException {
        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/crearEtiqueta.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage1 = new Stage();
        stage1.setTitle("Crear Etiqueta");
        stage1.sizeToScene();
        stage1.setResizable(false);
        stage1.setScene(scene);
        stage1.showAndWait();
    }

    public static void showConfiguracionMenu() throws IOException {
        ResourceBundle bundle = obtenerBundleActual();
        FXMLLoader fxmlLoader = new FXMLLoader(View.class.getResource("/view/menuConfiguracion.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);

        Stage stage1 = new Stage();
        stage1.setTitle("Configuracion");
        stage1.sizeToScene();
        stage1.setResizable(false);
        stage1.setScene(scene);
        stage1.showAndWait();
    }
}