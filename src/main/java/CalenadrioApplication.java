import Model.GestorTareas;
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
            this.primaryStage = stage;
            Platform.setImplicitExit(false);

            view.showInitialView();
            GestorTareas.getGestorTareas().verificarTareasHoy();

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                primaryStage.hide();
            });
            configurarSystemTray();

            primaryStage.show();
        }

    private void configurarSystemTray() {
        if (!SystemTray.isSupported()) {
            return; // Si el SO no lo soporta, no hacemos nada
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Imagen del icono (asegúrate de que la ruta sea correcta)
// Busca el icono en la raíz de los recursos, dentro de la carpeta View
            java.awt.Image image = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/View/icono.png")));
            PopupMenu menu = new PopupMenu();
            MenuItem abrirItem = new MenuItem("Abrir Gestor");
            MenuItem salirItem = new MenuItem("Salir");

            // Acciones: Recordar usar Platform.runLater para volver a JavaFX
            abrirItem.addActionListener(e -> Platform.runLater(() -> primaryStage.show()));
            salirItem.addActionListener(e -> {
                Platform.exit(); // Cierra JavaFX
                System.exit(0);  // Cierra el proceso JVM
            });

            menu.add(abrirItem);
            menu.add(salirItem);

            TrayIcon trayIcon = new TrayIcon(image, "Gestor de Tareas", menu);
            trayIcon.setImageAutoSize(true);

            // Acción si hacen doble clic en el icono del reloj
            trayIcon.addActionListener(e -> Platform.runLater(() -> primaryStage.show()));

            tray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}