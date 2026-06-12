package Controller;

import Model.GestorTareas;
import Model.Tarea;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.util.List;

public class MenuConfirmacionBorrarController {

    private Tarea tarea;
    @FXML
    private Button botonCancelar;

    @FXML
    private Button botonSoloUna;

    @FXML
    private Button  botonEliminarTodas;

    @FXML
    private void cancelarOp(){
        Stage ventanaActual = (Stage) botonCancelar.getScene().getWindow();
        ventanaActual.close();
    }

    public void setTareaMos(Tarea tarea){this.tarea=tarea;}
    @FXML
    private void borrarSoloEsta(){
        GestorTareas.getGestorTareas().eliminarTarea(tarea);
        cancelarOp();
    }

    @FXML
    private void borrarTodas(){
        String idF=tarea.getIdFamilia();
        GestorTareas gestorTareas=GestorTareas.getGestorTareas();
        List<Tarea> listBorrar=gestorTareas.getTodasTareas().stream().filter(tarea1 -> tarea1.getIdFamilia().equals(idF)).toList();
        for(Tarea tarea1 : listBorrar){
            gestorTareas.eliminarTarea(tarea1);
        }
        cancelarOp();
    }

    public void initialize(){

    }



}
