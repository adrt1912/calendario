package Model;

import java.awt.*;

public class NotificadorDeTareas {

    // Comprobamos si el sistema operativo soporta notificaciones
    public static void mostrarNotificacion(String titulo, String mensaje, Tarea tarea) {
        try {
            // Formateamos el mensaje incluyendo la hora de la tarea
            if(tarea.getHora()!=null) mensaje = mensaje + " a las " + tarea.getHora().toString();

            // Detectamos el sistema operativo de forma sencilla
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("linux")) {
                // Comando nativo para Linux
                ProcessBuilder pb = new ProcessBuilder("notify-send", "-u", "normal", titulo, mensaje);
                pb.start();
            } else if (os.contains("win")) {
                // Para Windows, se usa PowerShell para mostrar la notificación
                String psCommand = String.format("$title='%s'; $body='%s'; " +
                        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType=WindowsRuntime] | Out-Null; " +
                        "Write-Host 'Notificacion enviada';", titulo, mensaje);
                ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", psCommand);
                pb.start();
            }
        } catch (Exception e) {
            // Si algo falla, simplemente lo dejamos pasar para no bloquear la app
            System.err.println("No se pudo mostrar la notificación: " + e.getMessage());
        }
    }
}
