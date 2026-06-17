package View;

import Model.Tarea;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class view {
        //Guardamos la ventana del juego, asi se puede cambiar como se necesite
        private static Stage stage ;
    private static ResourceBundle obtenerBundleActual() {
        Preferences prefs = Preferences.userNodeForPackage(view.class);
        String codIdioma = prefs.get("idioma_actual", "es");
        Locale locale = new Locale(codIdioma);
        return ResourceBundle.getBundle("textos", locale);
    }
        //Ventana inicial
        public static void showInitialView() throws IOException {
            //Abre la ventana inicial

            ResourceBundle bundle=obtenerBundleActual();
            FXMLLoader fxmlLoader = new FXMLLoader(view.class.getResource("/View/MenuPrincipal.fxml"),bundle);
            Scene scene= new Scene(fxmlLoader.load(), 1500,800);

            //Si no está abierta se hace una nueva ventana
            if(stage==null){
                stage = new Stage();
            }
            //Se le da a la ventana un titulo y un escenario
            stage.setTitle("Calendario");
            stage.setScene(scene);
            stage.show();
        }


        public static void showCrearTArea(LocalDate fecha) throws IOException {
            ResourceBundle bundle=obtenerBundleActual();

            FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/crearTarea.fxml"),bundle);
            Scene scene=new Scene(fxmlLoader.load(),600,500);

            Controller.CrearTareaController controlador = fxmlLoader.getController();
            controlador.initialize(fecha);

            Stage stage1=new Stage();
            stage1.setTitle("Crear Tarea Nueva");
            stage1.setScene(scene);
            stage1.showAndWait();
        }

        public static void showTareaVentana(Tarea tarea)throws IOException{
            ResourceBundle bundle=obtenerBundleActual();

            FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/modificarTarea.fxml"),bundle);
            Scene scene=new Scene(fxmlLoader.load(),600,500);

            Controller.ModificarTareaController controlador = fxmlLoader.getController();
            controlador.setTareaMos(tarea);

            Stage stage1=new Stage();
            stage1.setTitle("Modificar Tarea ");
            stage1.setScene(scene);
            stage1.showAndWait();

        }
    public static void showConfirmacionEl(Tarea tarea)throws IOException{
        ResourceBundle bundle=obtenerBundleActual();

        FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/menuConfirmacionPer.fxml"),bundle);
        Scene scene=new Scene(fxmlLoader.load(),600,222);

        Controller.MenuConfirmacionBorrarController controlador = fxmlLoader.getController();
        controlador.setTareaMos(tarea);

        Stage stage1=new Stage();
        stage1.setTitle("Eliminar Tarea ");
        stage1.setScene(scene);
        stage1.showAndWait();
    }
    public static void showNuevaEtiqueta()throws IOException{
        ResourceBundle bundle=obtenerBundleActual();

        FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/crearEtiqueta.fxml"),bundle);
        Scene scene=new Scene(fxmlLoader.load(),600,232);

        Stage stage1=new Stage();
        stage1.setTitle("Crear Etiqueta ");
        stage1.setScene(scene);
        stage1.showAndWait();
        }
    public static void showConfiguracionMenu()throws IOException{
        ResourceBundle bundle=obtenerBundleActual();

        FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/menuConfiguracion.fxml"),bundle);
        Scene scene=new Scene(fxmlLoader.load(),600,600);

        Stage stage1=new Stage();
        stage1.setTitle("Configuracion ");
        stage1.setScene(scene);
        stage1.showAndWait();
    }
    }