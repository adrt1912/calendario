import View.view;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Objects;

public class CalenadrioApplication extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        // OBLIGATORIO: Registramos el Stage principal y el ciclo de vida antes de evaluar nada
        this.primaryStage = stage;
        Platform.setImplicitExit(false);
        view.setPrimaryStage(stage);

        // Inicializamos el SystemTray de fondo para que esté listo desde el segundo cero
        configurarSystemTray();
        Model.ConexionBD.getConexionBD().crearTablasSiNoExisten();
        //Cuenta por si acaso
        Model.ConexionBD.getConexionBD().registrarNuevoUsuario("safe", "1234");
        try {
            // Comportamiento de seguridad: Si cierran el Login sin meter el PIN, la app muere de verdad
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            // Al arrancar, vamos SIEMPRE al Login multiusuario para poder seleccionar perfil
            view.showPINInsert();

        } catch (Exception e) {
            System.err.println("Error catastrófico al cargar la pantalla de PIN: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void configurarSystemTray() {
        if (!SystemTray.isSupported()) return;
        try {SystemTray tray = SystemTray.getSystemTray();

            // Carga segura del icono de la aplicación
            java.awt.Image image = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/View/icono.png")));
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

            // Doble clic en el icono de la barra de herramientas restaura la ventana
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                if (primaryStage != null) {
                    primaryStage.show();
                    primaryStage.toFront();
                }
            }));
            tray.add(trayIcon);

        } catch (Exception e) {
            System.err.println("Aviso: No se pudo configurar el SystemTray: " + e.getMessage());
        }
    }
}