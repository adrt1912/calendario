package Model;

public enum Periodicidad {

    NUNCA(0,0,0),
    DIARIA(1,0,0),
    SEMANAL(7,0,0),
    QUINCENAL(15,0,0),
    MENSUAL(0,1,0),
    TRIMESTRAL(0,3,0),
    CUATRIMESTRAL(0,4,0),
    ANUAL(0,0,1);

    private final int dias;
    private final int mes;
    private final int anios;
    Periodicidad(int dias,int mes,int anios){
        this.dias=dias;
        this.mes=mes;
        this.anios=anios;
    }
//Devuelven el numero de tiempo de cada uno
    public int getAnios() {
        return anios;
    }

    public int getMes() {
        return mes;
    }

    public int getDias() {
        return dias;
    }
}