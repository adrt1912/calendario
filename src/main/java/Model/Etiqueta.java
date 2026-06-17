package Model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj){
        if(this ==obj) return true;
        if(obj instanceof Etiqueta){
            Etiqueta etiqueta=(Etiqueta) obj;
            if(etiqueta.getNombreEtiqueta().equals(nombreEtiqueta)) return  true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreEtiqueta);
    }
}