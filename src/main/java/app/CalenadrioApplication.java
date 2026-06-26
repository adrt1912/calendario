package app;

import View.view;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;

public class CalenadrioApplication extends Application {
    private Stage primaryStage;

    //Idea sonarcloud.io
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void start(Stage stage){
        // OBLIGATORIO: Registramos el Stage principal y el ciclo de vida antes de evaluar nada
        this.primaryStage = stage;
        Platform.setImplicitExit(false);
        view.setPrimaryStage(stage);


        // Inicializamos el SystemTray de fondo para que esté listo desde el segundo cero
        configurarSystemTray();
        model.ConexionBD.getConexionBD().crearTablasSiNoExisten();
        //Cuenta por si acaso
        model.ConexionBD.getConexionBD().registrarNuevoUsuario("safe", "1234");
        try {
            // Comportamiento de seguridad: Si cierran el Login sin meter el PIN, la app muere de verdad
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            // Al arrancar, vamos SIEMPRE al Login multiusuario para poder seleccionar perfil
            view.showPINInsert();

        } catch (Exception e) {
            logger.info("Error catastrófico al cargar la pantalla de PIN: " + e.getMessage());
        }
    }

    private void configurarSystemTray() {
        if (!SystemTray.isSupported()) return;
        try {SystemTray tray = SystemTray.getSystemTray();

            // Carga segura del icono de la aplicación
            java.awt.Image image = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/View/icono.png")));
            TrayIcon trayIcon = getTrayIcon(image);

            // Doble clic en el icono de la barra de herramientas restaura la ventana
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                if (primaryStage != null) {
                    primaryStage.show();
                    primaryStage.toFront();
                }
            }));
            tray.add(trayIcon);

        } catch (Exception e) {
            logger.info("Error al intentar cerrar sesión: "+ e.getMessage());
        }
    }

    private TrayIcon getTrayIcon(Image image) {
        PopupMenu menu = new PopupMenu();
        MenuItem abrirItem = new MenuItem("Abrir Gestor");
        MenuItem salirItem = new MenuItem("Salir");

        abrirItem.addActionListener(e -> Platform.runLater(() -> {
            if (primaryStage.isIconified()) primaryStage.setIconified(false);
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();
        }));

        salirItem.addActionListener(e -> {
            Platform.exit();
            System.exit(0);
        });

        menu.add(abrirItem);
        menu.add(salirItem);

        TrayIcon trayIcon = new TrayIcon(image, "Gestor de Tareas", menu);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    @Override
    public void stop(){
        // Fuerza el cierre de todos los hilos internos y libera la RAM en tu Linux
        model.GestorTareas.getGestorTareas().cerrarSesion();
        javafx.application.Platform.exit();
        System.exit(0);
    }
}