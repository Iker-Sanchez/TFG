package com.example.tfg;

/**
 * Estadísticas de un equipo para una temporada completa.
 * Campos que devuelve el backend en /estadisticas/:leagueId/:teamId
 */
public class EstadisticasEquipo {

    // Info básica del equipo
    public String nombre;
    public String escudo;

    // Resultados generales
    public int partidosJugados;
    public int victorias;
    public int empates;
    public int derrotas;

    // Goles
    public int golesFavor;
    public int golesContra;
    public int diferencia;

    // En casa
    public int victoriasLocal;
    public int empatesLocal;
    public int derrotasLocal;

    // Fuera
    public int victoriasVisitante;
    public int empatesVisitante;
    public int derrotasVisitante;

    // Tarjetas
    public int tarjetasAmarillas;
    public int tarjetasRojas;

    // Racha actual
    public String formaReciente;   // ej: "WDLWW"

    // Medias por partido
    public double mediaGolesFavor;
    public double mediaGolesContra;

    // Porcentaje victorias
    public int porcentajeVictorias;

    // Penaltis
    public int penaltisAFavor;
    public int penaltisEnContra;

    // Máxima racha ganando (en la temporada)
    public int maxRachaVictorias;
    public int maxRachaSinPerder;

}