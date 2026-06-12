import View.view;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.text.ViewFactory;
import java.util.Locale;
import java.util.prefs.Preferences;

public class CalenadrioApplication extends Application {

        @Override
        public void start(Stage stage) throws Exception {

            view.showInitialView();
        }

}
