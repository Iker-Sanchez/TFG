package com.example.tfg;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePremierActivity extends AppCompatActivity {

    private static final String TAG = "DetallePremier";

    private static final int ID_PREMIER = 39;
    private static final int COLOR_MORADO_PREMIER = 0xFF37003C;
    private static final int COLOR_VERDE_PREMIER = 0xFF00FF85;
    private static final int COLOR_ROSA_PREMIER = 0xFFFF2882;

    private LinearLayout panelClasificacion;
    private LinearLayout panelPartidos;
    private TextView tabClasificacion;
    private TextView tabPartidos;
    private LinearLayout contenedorFilas;
    private LinearLayout contenedorPartidos;
    private Button btnSelectorJornada;

    private int idLiga = ID_PREMIER;
    private int jornadaSeleccionada = 1;
    private int totalJornadas = 38;
    private boolean clasificacionCargada = false;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premier_detalle);

        bindViews();
        setupListeners();

        apiService = RetrofitClient.getClient().create(ApiService.class);

        cargarClasificacion();
        cargarJornadaActual();
    }

    private void bindViews() {
        panelClasificacion = findViewById(R.id.panelClasificacion);
        panelPartidos = findViewById(R.id.panelPartidos);
        tabClasificacion = findViewById(R.id.tabClasificacion);
        tabPartidos = findViewById(R.id.tabPartidos);
        contenedorFilas = findViewById(R.id.contenedorFilas);
        contenedorPartidos = findViewById(R.id.contenedorPartidos);
        btnSelectorJornada = findViewById(R.id.btnSelectorJornada);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSelectorJornada.setBackgroundColor(COLOR_VERDE_PREMIER);
        btnSelectorJornada.setTextColor(COLOR_MORADO_PREMIER);
    }

    private void setupListeners() {
        tabClasificacion.setOnClickListener(v -> mostrarPanelClasificacion());
        tabPartidos.setOnClickListener(v -> mostrarPanelPartidos());
        btnSelectorJornada.setOnClickListener(v -> abrirSelectorJornada());
    }

    private void mostrarPanelClasificacion() {
        panelClasificacion.setVisibility(View.VISIBLE);
        panelPartidos.setVisibility(View.GONE);

        tabClasificacion.setTextColor(COLOR_VERDE_PREMIER);
        tabPartidos.setTextColor(0xFF999999);
    }

    private void mostrarPanelPartidos() {
        panelClasificacion.setVisibility(View.GONE);
        panelPartidos.setVisibility(View.VISIBLE);

        tabPartidos.setTextColor(COLOR_VERDE_PREMIER);
        tabClasificacion.setTextColor(0xFF999999);
    }

    private void cargarJornadaActual() {
        actualizarBotonJornada();

        apiService.getJornadaActual(idLiga).enqueue(new Callback<JornadaActual>() {
            @Override
            public void onResponse(Call<JornadaActual> call, Response<JornadaActual> response) {
                if (response.isSuccessful() && response.body() != null && response.body().jornada > 0) {
                    jornadaSeleccionada = response.body().jornada;
                    Log.d(TAG, "Jornada actual Premier: " + jornadaSeleccionada);
                }

                actualizarBotonJornada();
                cargarPartidosJornada(jornadaSeleccionada);
            }

            @Override
            public void onFailure(Call<JornadaActual> call, Throwable t) {
                Log.e(TAG, "Error jornada Premier: " + t.getMessage());
                actualizarBotonJornada();
                cargarPartidosJornada(jornadaSeleccionada);
            }
        });
    }

    private void actualizarBotonJornada() {
        runOnUiThread(() ->
                btnSelectorJornada.setText("↓  Jornada " + jornadaSeleccionada)
        );
    }

    private void cargarClasificacion() {
        if (clasificacionCargada) return;

        apiService.getClasificacion(idLiga).enqueue(new Callback<List<Clasificacion>>() {
            @Override
            public void onResponse(Call<List<Clasificacion>> call, Response<List<Clasificacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clasificacionCargada = true;
                    mostrarClasificacion(response.body());
                } else {
                    Log.e(TAG, "Error clasificación Premier HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Clasificacion>> call, Throwable t) {
                Log.e(TAG, "Fallo clasificación Premier: " + t.getMessage());
            }
        });
    }

    private void mostrarClasificacion(List<Clasificacion> equipos) {
        LayoutInflater inflater = LayoutInflater.from(this);
        contenedorFilas.removeAllViews();

        for (int i = 0; i < equipos.size(); i++) {
            Clasificacion eq = equipos.get(i);

            View fila = inflater.inflate(
                    R.layout.item_premier_clasificacion,
                    contenedorFilas,
                    false
            );

            fila.findViewById(R.id.filaEquipo)
                    .setBackgroundColor(i % 2 == 0 ? 0xFF120016 : 0xFF1D0023);

            TextView txtPuesto = fila.findViewById(R.id.txtPuesto);
            txtPuesto.setText(String.valueOf(eq.puesto));

            if (eq.puesto <= 4) {
                txtPuesto.setTextColor(COLOR_VERDE_PREMIER);
            } else if (eq.puesto <= 6) {
                txtPuesto.setTextColor(COLOR_ROSA_PREMIER);
            } else if (eq.puesto >= 18) {
                txtPuesto.setTextColor(0xFFE53935);
            } else {
                txtPuesto.setTextColor(0xFFFFFFFF);
            }

            ImageView imgEscudo = fila.findViewById(R.id.imgEscudo);
            if (eq.escudo != null && !eq.escudo.isEmpty()) {
                Glide.with(this)
                        .load(eq.escudo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imgEscudo);
            }

            ((TextView) fila.findViewById(R.id.txtNombre)).setText(eq.nombre);
            ((TextView) fila.findViewById(R.id.txtPJ)).setText(String.valueOf(eq.pj));
            ((TextView) fila.findViewById(R.id.txtV)).setText(String.valueOf(eq.pg));
            ((TextView) fila.findViewById(R.id.txtE)).setText(String.valueOf(eq.pe));
            ((TextView) fila.findViewById(R.id.txtD)).setText(String.valueOf(eq.pp));
            ((TextView) fila.findViewById(R.id.txtGF)).setText(String.valueOf(eq.gf));
            ((TextView) fila.findViewById(R.id.txtGC)).setText(String.valueOf(eq.gc));

            String dg = eq.dg > 0 ? "+" + eq.dg : String.valueOf(eq.dg);
            ((TextView) fila.findViewById(R.id.txtDG)).setText(dg);
            ((TextView) fila.findViewById(R.id.txtPuntos)).setText(String.valueOf(eq.puntos));

            contenedorFilas.addView(fila);
        }
    }

    private void cargarPartidosJornada(int jornada) {
        contenedorPartidos.removeAllViews();
        mostrarMensaje("Cargando partidos de Premier...", 0xFFAAAAAA);

        apiService.getPartidosJornada(idLiga, jornada).enqueue(new Callback<List<Partido>>() {
            @Override
            public void onResponse(Call<List<Partido>> call, Response<List<Partido>> response) {
                contenedorPartidos.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    List<Partido> partidos = response.body();

                    if (partidos.isEmpty()) {
                        mostrarMensaje("No hay partidos disponibles para esta jornada", 0xFF999999);
                    } else {
                        mostrarPartidos(partidos);
                    }
                } else {
                    mostrarMensaje("No se pudieron cargar los partidos", 0xFFFF6B6B);
                }
            }

            @Override
            public void onFailure(Call<List<Partido>> call, Throwable t) {
                Log.e(TAG, "Fallo partidos Premier: " + t.getMessage());
                contenedorPartidos.removeAllViews();
                mostrarMensaje("Sin conexión con el servidor", 0xFFFF6B6B);
            }
        });
    }

    private void mostrarPartidos(List<Partido> partidos) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Partido p : partidos) {
            View fila = inflater.inflate(
                    R.layout.item_premier_partido,
                    contenedorPartidos,
                    false
            );

            ImageView imgLocal = fila.findViewById(R.id.imgEscudoLocal);
            ImageView imgVisitante = fila.findViewById(R.id.imgEscudoVisitante);

            TextView txtNombreLocal = fila.findViewById(R.id.txtNombreLocal);
            TextView txtNombreVisitante = fila.findViewById(R.id.txtNombreVisitante);

            cargarEscudo(imgLocal, p.localEscudo);
            cargarEscudo(imgVisitante, p.visitanteEscudo);

            txtNombreLocal.setText(p.localNombre);
            txtNombreVisitante.setText(p.visitanteNombre);

            imgLocal.setOnClickListener(v ->
                    abrirEstadisticas(p.localTeamId, p.localNombre, p.localEscudo)
            );

            imgVisitante.setOnClickListener(v ->
                    abrirEstadisticas(p.visitanteTeamId, p.visitanteNombre, p.visitanteEscudo)
            );

            View centro = fila.findViewById(R.id.layoutCentroPartido);
            if (centro != null) {
                centro.setOnClickListener(v -> abrirPartidoDetalle(p));
            }

            ((TextView) fila.findViewById(R.id.txtFecha)).setText(p.fecha);
            ((TextView) fila.findViewById(R.id.txtHora)).setText(p.hora);

            boolean tieneResultado = p.golesLocal != null && p.golesVisitante != null;
            View layoutResultado = fila.findViewById(R.id.layoutResultado);

            if (tieneResultado) {
                layoutResultado.setVisibility(View.VISIBLE);
                ((TextView) fila.findViewById(R.id.txtGolesLocal)).setText(String.valueOf(p.golesLocal));
                ((TextView) fila.findViewById(R.id.txtGolesVisitante)).setText(String.valueOf(p.golesVisitante));

                if ("1H".equals(p.estado) || "2H".equals(p.estado) || "HT".equals(p.estado)) {
                    TextView txtHora = fila.findViewById(R.id.txtHora);
                    txtHora.setTextColor(COLOR_ROSA_PREMIER);
                    txtHora.setText("EN VIVO");
                }
            } else {
                layoutResultado.setVisibility(View.GONE);
            }

            contenedorPartidos.addView(fila);
        }
    }

    private void abrirPartidoDetalle(Partido p) {
        Intent intent = new Intent(this, PrediccionActivity.class);

        intent.putExtra("FIXTURE_ID", p.fixtureId);
        intent.putExtra("LEAGUE_ID", 39);

        intent.putExtra("LOCAL_TEAM_ID", p.localTeamId);
        intent.putExtra("VISITANTE_TEAM_ID", p.visitanteTeamId);

        intent.putExtra("LOCAL_NOMBRE", p.localNombre);
        intent.putExtra("LOCAL_ESCUDO", p.localEscudo);
        intent.putExtra("VISITANTE_NOMBRE", p.visitanteNombre);
        intent.putExtra("VISITANTE_ESCUDO", p.visitanteEscudo);

        startActivity(intent);
    }

    private void abrirEstadisticas(int teamId, String nombre, String escudo) {
        android.content.Intent intent = new android.content.Intent(this, EstadisticasEquipoActivity.class);
        intent.putExtra("TEAM_ID", teamId);
        intent.putExtra("LEAGUE_ID", idLiga);
        intent.putExtra("TEAM_NOMBRE", nombre);
        intent.putExtra("TEAM_ESCUDO", escudo);
        startActivity(intent);
    }

    private void cargarEscudo(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imageView);
        }
    }

    private void mostrarMensaje(String mensaje, int color) {
        runOnUiThread(() -> {
            TextView tv = new TextView(this);
            tv.setText(mensaje);
            tv.setTextColor(color);
            tv.setTextSize(14f);
            tv.setPadding(40, 60, 40, 0);
            contenedorPartidos.addView(tv);
        });
    }

    private void abrirSelectorJornada() {
        String[] opciones = new String[totalJornadas];

        for (int i = 0; i < totalJornadas; i++) {
            opciones[i] = "Jornada " + (i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una jornada Premier")
                .setItems(opciones, (dialog, which) -> {
                    jornadaSeleccionada = which + 1;
                    actualizarBotonJornada();
                    cargarPartidosJornada(jornadaSeleccionada);
                })
                .show();
    }
}