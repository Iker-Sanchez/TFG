package com.example.tfg;

public class Partido {

    public int     fixtureId;        // ID del partido en la API
    public String  jornada;
    public String  fecha;
    public String  hora;
    public String  estado;           // "NS" = por jugar, "FT" = terminado, "1H","2H","HT" = en curso

    public int     localTeamId;
    public String  localNombre;
    public String  localEscudo;

    public int     visitanteTeamId;
    public String  visitanteNombre;
    public String  visitanteEscudo;

    public Integer golesLocal;
    public Integer golesVisitante;
}