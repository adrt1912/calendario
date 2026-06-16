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

    //Trasnforma el codigo "es" al idioma correspondiente
    public static Idiomas desdeCodigo(String codigoBuscado) {
        for (Idiomas idioma : Idiomas.values()) {
            if (idioma.getCodigo().equalsIgnoreCase(codigoBuscado)) {
                return idioma;
            }
        }
        return ESPAÑOL;
    }
}
