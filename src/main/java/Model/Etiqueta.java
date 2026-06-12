package Model;

public class Etiqueta {


    private String codColor;
    private String nombreEtiqueta;


    public Etiqueta(String nombreEtiqueta,String codColor){
        this.codColor=codColor;
        this.nombreEtiqueta=nombreEtiqueta;
    }

    public String getCodColor() {
        return codColor;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    @Override
    public String toString(){
        return nombreEtiqueta;
    }
}
