package Model;

import java.util.Objects;

public record Etiqueta(String nombreEtiqueta, String codColor) {

    @Override
    public String toString() {
        return nombreEtiqueta;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Etiqueta etiqueta) return etiqueta.nombreEtiqueta().equals(nombreEtiqueta);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreEtiqueta);
    }
}