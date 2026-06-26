package model;

public enum Idiomas {
    Espaniol("es", "Español"),
    INGLES("en", "English"),
    FRANCES("fr", "Français"),
    EUSKERA("eu", "Euskara"),
    ALEMAN("de", "Deutsch");

     Idiomas(String codigo,String nombreVisible){
        this.codigo=codigo;
        this.nombreVisible=nombreVisible;
    }
    private final String codigo;
    private final String nombreVisible;

    public String getCodigo() {
        return codigo;
    }

    @Override
    public String toString() {return nombreVisible;}

    //Trasnforma el codigo "es" al idioma correspondiente
    public static Idiomas desdeCodigo(String codigoBuscado) {
        for (Idiomas idioma : Idiomas.values()) {
            if (idioma.getCodigo().equalsIgnoreCase(codigoBuscado)) {
                return idioma;
            }
        }
        return Espaniol;
    }
}
