package model;

import java.time.LocalDate;
import java.time.LocalTime;

public record TareaDatos(
        String titulo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String descripcion,
        String sitio,
        LocalTime timeInicial,
        LocalTime horaFin,
        Periodicidad frecuencia,
        String idFamilia,
        Etiqueta etiqueta
) {}