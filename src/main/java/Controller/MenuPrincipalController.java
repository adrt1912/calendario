package Controller;

import Model.EstadoTarea;
import Model.GestorTareas;
import Model.Periodicidad;
import Model.Tarea;
import View.view;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.List;

public class MenuPrincipalController {

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    //Guardamos la fecha que se muestra por pantalla
    LocalDate fechaSeleccionada=LocalDate.now();
    @FXML
    private GridPane calendario;

    private VBox[][] calendarioVBox=new VBox[7][7];

    @FXML
    private ScrollPane mostradorTareas;

    @FXML
    private Text cartelAño;

    @FXML
    private Text cartelMes;

    @FXML
    private Text textFecha;

    private Tarea tareaSelec;

    //Para escribir el titulo
   private final String[] semana= {"Lunes","Martes","Miercoles","Jueves","Viernes","Sábado","Domingo"};
   // private String[] mes={"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};

    public void initialize(){
        gestorTareas.iniciarGestor();

        iniciarMatrizVBox();
        mostrarCalendario();
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());
    }
    private void iniciarMatrizVBox(){

        for(int i=0;i<7;i++){
            for (int j=0;j<7;j++){
                calendarioVBox[j][i]=new VBox();
                int finalJ = j;
                int finalI = i;
                calendarioVBox[j][i].setOnMouseClicked(event -> {
                    int dia=((finalI-1)*7)+finalJ-LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue()+2;
                    fechaSeleccionada=LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonth(),dia);
                    mostrarTareas();
                });
            }
        }
    }

    private void mostrarTareas(){
        textFecha.setText(fechaSeleccionada.toString());
        List<Tarea> listaTareasMostrar=gestorTareas.getTodasTareas().stream().filter(tarea -> fechaSeleccionada.equals(tarea.getFechaFin())).toList();
        VBox vBox=new VBox();
        vBox.setSpacing(5);
        int i=1;
        for (Tarea tarea : listaTareasMostrar) {
            Label text = new Label(i+": "+tarea.mostrarTarea());
            text.setFont(Font.font(12));
            text.setCursor(Cursor.HAND);
           vBox.getChildren().add(text);
           i++;

           text.setOnMouseClicked(event -> {
               try{
                   view.showTareaVentana(tarea);
                   mostrarCalendario();
                   mostrarTareas();
               } catch (Exception ignored) {
                   throw new RuntimeException(ignored);

               }
           });
        }
        mostradorTareas.setContent(vBox);
    }

    public void mostrarCalendario(){
        //Para borrar lo que hay escrito en el calendario
        calendario.getChildren().clear();
        iniciarMatrizVBox();

        //Se obtene la cantidad de dias del mes
        int numDiasMes=fechaSeleccionada.lengthOfMonth();
        //Para ver que dia empieza el mes
        int fechaPrimerDiaMes=LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();

        cartelMes.setText(" "+fechaSeleccionada.getMonth().name());
        cartelAño.setText(""+fechaSeleccionada.getYear());

        int numMes=1;
        for(int i=0;i<calendario.getRowCount();i++){
            for (int j=0;j< calendario.getColumnCount();j++){
                calendario.add(calendarioVBox[j][i], j, i);

                if(i==0){
                     calendarioVBox[j][i].getChildren().add(new Label(semana[j]));}
                else{
                    if(i==1){
                        if(j>=fechaPrimerDiaMes-1){
                            calendarioVBox[j][i].getChildren().add(new Label(numMes+""));
                            numMes++;
                        }
                    }else{
                        if(numMes<=numDiasMes){
                            calendarioVBox[j][i].getChildren().add(new Label(numMes+""));
                        numMes++;
                        }
                    }
                }
            }
        }
        mostrarEtiquetas();
    }
    private void mostrarEtiquetas(){
        List<Tarea> listaTareas=gestorTareas.getTodasTareas();
        int primerDiaMes=LocalDate.of(fechaSeleccionada.getYear(),fechaSeleccionada.getMonthValue(), 1).getDayOfWeek().getValue();
        for (Tarea tarea : listaTareas) {
            LocalDate fecha = tarea.getFechaFin();
            if (fecha.getMonth().equals(fechaSeleccionada.getMonth())&&fecha.getYear()==fechaSeleccionada.getYear()) {
                String titulo=tarea.getNombreTarea();
                int pos=(fecha.getDayOfMonth()-2)+primerDiaMes;
                int columna=pos%7;
                int fila=(pos/7)+1;
                if(tarea.getEstadoTarea()!= EstadoTarea.EN_PROCESO){
                    Label label=new Label((titulo));
                    label.setOpacity(0.2);
                calendarioVBox[columna][fila].getChildren().add(label);
                }else{
                    Label label=new Label((titulo));
                    label.setOpacity(1);
                    calendarioVBox[columna][fila].getChildren().add(label);
                }
            }
        }
    }

    @FXML
    private void retrocederMes(){
        fechaSeleccionada=fechaSeleccionada.minusMonths(1);
        mostrarCalendario();
    }

    @FXML
    private void pasarMes(){
        fechaSeleccionada=fechaSeleccionada.plusMonths(1);
        mostrarCalendario();
    }

    @FXML
    private void añadirEvento() {
        try {
            view.showCrearTArea();
            mostrarCalendario();
        } catch (Exception ignored) {}
    }

    @FXML
    private Text TareasPendientesHoy;

    @FXML
    private Text TareasPendientesMañana;

}