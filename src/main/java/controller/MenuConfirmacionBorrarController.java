package controller;

import model.GestorTareas;
import model.Tarea;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.List;
import java.util.prefs.Preferences;

public class MenuConfirmacionBorrarController {

    private Tarea tarea;
    @FXML
    private Button botonCancelar;

    @FXML
    private AnchorPane rootPane;

    //Initialize que pone el idioma y el color
    public void initialize(){
        Preferences prefs = Preferences.userNodeForPackage(GestorTareas.class);
        if (prefs.getBoolean("modo_oscuro", false) && rootPane != null) {
            rootPane.getStyleClass().add("dark-mode");
        }
    }

    //Solo cierra la ventana, como si nada hubiera pasado
    @FXML
    private void cancelarOp(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        if(ventanaActual!=null)  ventanaActual.close();
    }

    //Guarda la tarea que se quiere borrar, para poder usarla
    public void setTareaMos(Tarea tarea){this.tarea=tarea;}
    //Solo elimina esta tarea
    @FXML
    private void borrarSoloEsta(){
        if(tarea!=null) GestorTareas.getGestorTareas().eliminarTarea(tarea);
        cancelarOp();
    }

    //Borra todas las tareas de esta familia, filtrandolas por su idFamilia
    @FXML
    private void borrarTodas(){
        if (tarea != null && tarea.getIdFamilia() != null) {
        String idF=tarea.getIdFamilia();
        GestorTareas gestorTareas=GestorTareas.getGestorTareas();
        List<Tarea> listBorrar=gestorTareas.getTodasTareas().stream().filter(tarea1 -> tarea1.getIdFamilia()!=null &&tarea1.getIdFamilia().equals(idF)).toList();

        for(Tarea tarea1 : listBorrar)gestorTareas.eliminarTarea(tarea1);
        cancelarOp();
        }
    }
}