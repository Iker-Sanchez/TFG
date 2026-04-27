package com.example.tfg;

/**
 * Modelo de predicción de un partido.
 * Campos que devuelve el backend en /prediccion/:fixtureId
 */
public class PrediccionPartido {

    // Equipos
    public String localNombre;
    public String localEscudo;
    public String visitanteNombre;
    public String visitanteEscudo;

    // Predicción del ganador
    public String ganadorNombre;    // nombre del equipo favorito o "Empate"
    public String consejo;          // consejo de apuesta (advice)

    // Porcentajes de resultado
    public String pctLocal;         // "55%"
    public String pctEmpate;        // "25%"
    public String pctVisitante;     // "20%"

    // Goles esperados
    public String golesEsperadosLocal;      // "-1.5" etc
    public String golesEsperadosVisitante;

    // Comparativa (últimos 5 partidos)
    public String formaLocal;           // "75%"
    public String formaVisitante;       // "25%"
    public String ataqueLocal;          // "60%"
    public String ataqueVisitante;      // "40%"
    public String defensaLocal;         // "55%"
    public String defensaVisitante;     // "45%"
    public String h2hLocal;             // historial cara a cara local
    public String h2hVisitante;
    public String totalLocal;           // puntuación total
    public String totalVisitante;

    // Estadísticas de temporada local (últimos 5)
    public double mediaGolesLocalMarca;
    public double mediaGolesLocalRecibe;
    public double mediaGolesVisitanteMarca;
    public double mediaGolesVisitanteRecibe;

    // Forma reciente (últimos 5 resultados como string "WWDLW")
    public String formaRecienteLocal;
    public String formaRecienteVisitante;

    // Tarjetas medias por partido
    public double amarillasLocal;
    public double amarillasVisitante;

    // H2H últimos partidos
    public String[] h2hResultados;  // ["3-1", "0-2", "1-1"] etc
}