package Controller;

import Model.GestorTareas;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.List;

public class MenuPrincipalController {

    GestorTareas gestorTareas=GestorTareas.getGestorTareas();

    LocalDate fechaActual=LocalDate.now();
    LocalDate fechaSeleccionada=LocalDate.now();
    private int mesMostrando=fechaActual.getMonthValue();

    private int añoMostrando=fechaActual.getYear();

    @FXML
    private GridPane calendario;

    @FXML
    private Text cartelAño;

    @FXML
    private Text cartelMes;


    private String[] semana= {"Lunes","Martes","Miercoles","Jueves","Viernes","Sábado","Domingo"};

    private String[] mes={"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};



    public void initialize(){
        mostrarCalendario();
        TareasPendientesHoy.setText(gestorTareas.mostrarTareasUrgentesHoy());
        TareasPendientesMañana.setText(gestorTareas.mostrarTareasUrgentesMañana());
    }

    public void mostrarCalendario(){
        calendario.getChildren().clear();

        añoMostrando=fechaSeleccionada.getYear();
        mesMostrando=fechaSeleccionada.getMonthValue();
        int numDiasMes=fechaSeleccionada.lengthOfMonth();
        int fechaPrimerDiaMes=LocalDate.of(añoMostrando,mesMostrando, 1).getDayOfWeek().getValue();

        cartelMes.setText(" "+fechaSeleccionada.getMonth().name());
        cartelAño.setText(""+fechaSeleccionada.getYear());

        int numMes=1;
        for(int i=0;i<calendario.getRowCount();i++){
            for (int j=0;j< calendario.getColumnCount();j++){
                if(i==0){
                    calendario.add(new javafx.scene.control.Label(semana[j]),j,i);
                }
                else{
                    if(i==1){
                        if(j>=fechaPrimerDiaMes-1){
                            calendario.add(new javafx.scene.control.Label(numMes+""),j,i);
                            numMes++;
                        }
                    }else{
                        if(numMes<=numDiasMes){
                        calendario.add(new javafx.scene.control.Label(numMes+""),j,i);
                        numMes++;
                        }
                    }
                }
            }
        }

    }


    //EStos dos metodos hay que cambiar, segun configuracion tal, version inicial
private void iniciarSemanaIdioma(){
    semana[0]="Lunes";
    semana[1]="Martes";
    semana[2]="Miercoles";
    semana[3]="Jueves";
    semana[4]="Viernes";
    semana[5]="Sabado";
    semana[6]="Domingo";
}

private void iniciarMesIdioma(){
    mes[0]="Enero";
    mes[1]="Febrero";
    mes[2]="Marzo";
    mes[3]="Abril";
    mes[4]="Mayo";
    mes[5]="Junio";
    mes[6]="Julio";
    mes[7]="Agosto";
    mes[8]="Septiembre";
    mes[9]="Octubre";
    mes[10]="Noviembre";
    mes[11]="Diciembre";


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
    private void añadirEvento(){
        gestorTareas.anadirTarea();
    }
    @FXML
    private void eliminarTarea(){
        gestorTareas.eliminarTarea();
    }

    @FXML
    private void modificarTarea(){
        gestorTareas.modificarTarea();
    }
    @FXML
    private void completarTarea(){
        gestorTareas.completarTarea();
    }

    @FXML
    private void guardarDatos(){}

    @FXML
    private void descargarDatos(){}

    @FXML
    private void limpiarFichero(){}

    @FXML
    private Text TareasPendientesHoy;

    @FXML
    private Text TareasPendientesMañana;


}