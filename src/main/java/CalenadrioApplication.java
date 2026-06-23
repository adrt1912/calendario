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
            view.setPrimaryStage(stage);
            view.showInitialView();
            GestorTareas.getGestorTareas().verificarTareasHoy();

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                primaryStage.hide();
            });
            configurarSystemTray();
        }

    private void configurarSystemTray() {
        if (!SystemTray.isSupported()) return; // Si el SO no lo soporta, no hacemos nada
        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Imagen del icono (asegúrate de que la ruta sea correcta)
            java.awt.Image image = javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/View/icono.png")));
            PopupMenu menu = new PopupMenu();
            MenuItem abrirItem = new MenuItem("Abrir Gestor");
            MenuItem salirItem = new MenuItem("Salir");

            // Acciones: Recordar usar Platform.runLater para volver a JavaFX
            abrirItem.addActionListener(e -> Platform.runLater(() -> {
                if (primaryStage.isIconified()) primaryStage.setIconified(false); // Si estaba minimizado en la barra, lo restaura
                primaryStage.show();
                primaryStage.toFront(); // Lo trae al frente para que no se quede detrás de otras ventanas
                primaryStage.requestFocus(); // Fuerza el foco para que no se vea blanco
            }));

            menu.add(abrirItem);
            menu.add(salirItem);

            TrayIcon trayIcon = new TrayIcon(image, "Gestor de Tareas", menu);
            trayIcon.setImageAutoSize(true);

            // Acción si hacen doble clic en el icono del reloj
            trayIcon.addActionListener(e -> Platform.runLater(() -> primaryStage.show()));

            tray.add(trayIcon);
            salirItem.addActionListener(e -> {
                Platform.exit(); // Cierra el entorno JavaFX de forma segura
                System.exit(0);  // Mata el proceso de la Máquina Virtual de Java (JVM)
            });

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}