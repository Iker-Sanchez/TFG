package com.example.tfg;

import java.util.List;

public class OnceProbable {
    public String     formacion;   // "4-3-3"
    public List<Jugador> jugadores;

    public static class Jugador {
        public int    id;        // player ID de la API
        public String nombre;
        public int    numero;
        public String posicion;  // "G", "D", "M", "F"
        public int    minutos;
        public int    partidos;
        public int    titular;
        public String foto;      // URL foto del jugador
    }
}