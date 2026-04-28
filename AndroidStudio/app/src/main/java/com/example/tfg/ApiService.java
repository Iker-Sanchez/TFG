package com.example.tfg;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("clasificacion/laliga")
    Call<List<Clasificacion>> getClasificacionLaLiga();

    @GET("clasificacion/{leagueId}")
    Call<List<Clasificacion>> getClasificacion(@Path("leagueId") int leagueId);

    @GET("jornada-actual/{leagueId}")
    Call<JornadaActual> getJornadaActual(@Path("leagueId") int leagueId);

    @GET("partidos/{leagueId}/{jornada}")
    Call<List<Partido>> getPartidosJornada(
            @Path("leagueId") int leagueId,
            @Path("jornada")  int jornada
    );

    @GET("estadisticas/{leagueId}/{teamId}")
    Call<EstadisticasEquipo> getEstadisticasEquipo(
            @Path("leagueId") int leagueId,
            @Path("teamId")   int teamId
    );

    @GET("prediccion/{fixtureId}")
    Call<PrediccionPartido> getPrediccion(@Path("fixtureId") int fixtureId);

    @GET("estadisticas-partido/{fixtureId}")
    Call<EstadisticasPartido> getEstadisticasPartido(@Path("fixtureId") int fixtureId);

    @GET("once-probable/{leagueId}/{teamId}/{fixtureId}")
    Call<OnceProbable> getOnceProbable(
            @Path("leagueId")  int leagueId,
            @Path("teamId")    int teamId,
            @Path("fixtureId") int fixtureId
    );

    @GET("estadisticas-jugador/{playerId}/{leagueId}")
    Call<EstadisticasJugador> getEstadisticasJugador(
            @Path("playerId")  int playerId,
            @Path("leagueId")  int leagueId
    );

    @GET("buscar-jugadores/{nombre}")
    Call<BusquedaJugadores> buscarJugadores(
            @Path("nombre") String nombre,
            @Query("season") int season
    );
}