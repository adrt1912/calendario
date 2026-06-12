package Model;

public enum Idiomas {
    ESPAÑOL("es"),
    INGLES("en");

    private Idiomas(String codigo){
        this.codigo=codigo;
    }
    private final String codigo;

    public String getCodigo() {
        return codigo;
    }
}
