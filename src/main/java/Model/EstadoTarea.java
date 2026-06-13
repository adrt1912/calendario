package Model;

public enum EstadoTarea {

    COMPLETADA("COMPLETADA"),
    EN_PROCESO("EN_PROCESO"),
    CADUCADA("CADUCADA");

    private final String nom;

    public String getNom() {
        return nom;
    }
    EstadoTarea(String nom){
        this.nom=nom;
    }
}
