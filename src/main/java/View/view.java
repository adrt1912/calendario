package View;

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
            Scene scene= new Scene(fxmlLoader.load(), 1300,800);

            //Si no está abierta se hace una nueva ventana
            if(stage==null){
                stage = new Stage();
            }
            //Se le da a la ventana un titulo y un escenario
            stage.setTitle("Calendario");
            stage.setScene(scene);
            stage.show();
        }
    }