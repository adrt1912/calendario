package View;

import Model.Tarea;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class view {
        //Guardamos la ventana del juego, asi se puede cambiar como se necesite
        private static Stage stage ;

        //Ventana inicial
        public static void showInitialView() throws IOException {
            //Abre la ventana inicial
            FXMLLoader fxmlLoader = new FXMLLoader(view.class.getResource("/View/MenuPrincipal.fxml"));
            Scene scene= new Scene(fxmlLoader.load(), 1600,900);

            //Si no está abierta se hace una nueva ventana
            if(stage==null){
                stage = new Stage();
            }
            //Se le da a la ventana un titulo y un escenario
            stage.setTitle("Calendario");
            stage.setScene(scene);
            stage.show();
        }


        public static void showCrearTArea() throws IOException {
            FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/crearTarea.fxml"));
            Scene scene=new Scene(fxmlLoader.load(),600,500);

            Stage stage1=new Stage();
            stage1.setTitle("Crear Tarea Nueva");
            stage1.setScene(scene);
            stage1.showAndWait();
        }

        public static void showTareaVentana(Tarea tarea)throws IOException{
            FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/modificarTarea.fxml"));
            Scene scene=new Scene(fxmlLoader.load(),600,500);

            Controller.ModificarTareaController controlador = fxmlLoader.getController();
            controlador.setTareaMos(tarea);

            Stage stage1=new Stage();
            stage1.setTitle("Modificar Tarea ");
            stage1.setScene(scene);
            stage1.showAndWait();

        }
    public static void showConfirmacionEl(Tarea tarea)throws IOException{
        FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/menuConfirmacionPer.fxml"));
        Scene scene=new Scene(fxmlLoader.load(),600,222);

        Controller.MenuConfirmacionBorrarController controlador = fxmlLoader.getController();
        controlador.setTareaMos(tarea);

        Stage stage1=new Stage();
        stage1.setTitle("Eliminar Tarea ");
        stage1.setScene(scene);
        stage1.showAndWait();
    }
    public static void showNuevaEtiqueta()throws IOException{
        FXMLLoader fxmlLoader=new FXMLLoader(view.class.getResource("/View/crearEtiqueta.fxml"));
        Scene scene=new Scene(fxmlLoader.load(),600,232);

        Stage stage1=new Stage();
        stage1.setTitle("Crear Etiqueta ");
        stage1.setScene(scene);
        stage1.showAndWait();
        }
    }