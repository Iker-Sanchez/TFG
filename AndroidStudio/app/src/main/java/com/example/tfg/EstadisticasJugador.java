package com.example.tfg;

/**
 * Estadísticas de un jugador en una liga/temporada concreta.
 * Devuelto por /estadisticas-jugador/:playerId/:leagueId
 */
public class EstadisticasJugador {

    // Info del jugador
    public String nombre;
    public String foto;        // URL foto del jugador
    public String equipo;
    public String escudoEquipo;
    public String posicion;
    public int    numero;
    public String nacionalidad;
    public int    edad;
    public String valoracion;  // "7.85"

    // Participación
    public int    partidos;
    public int    titularidades;
    public int    minutos;

    // Ataque
    public int    goles;
    public int    asistencias;
    public int    tirosTotales;
    public int    tirosPuerta;

    // Pases
    public int    pasesTotales;
    public int    pasesClave;
    public int    precisionPases; // %

    // Defensa
    public int    tacklesTotales;
    public int    intercepciones;
    public int    bloqueOS;

    // Duelos y regates
    public int    duelosTotales;
    public int    duelosGanados;
    public int    regatesIntentados;
    public int    regatesExitosos;

    // Disciplina
    public int    faltasCometidas;
    public int    faltasRecibidas;
    public int    tarjetasAmarillas;
    public int    tarjetasRojas;

    // Penaltis
    public int    penaltisGanados;
    public int    penaltisConvertidos;
    public int    penaltisFallados;
}