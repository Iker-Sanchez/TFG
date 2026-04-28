package com.example.tfg;

import android.app.AlertDialog;
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

public class DetalleLigaActivity extends AppCompatActivity {

    private static final String TAG = "DetalleLiga";

    // ---- Vistas ----
    private LinearLayout panelClasificacion;
    private LinearLayout panelPartidos;
    private TextView     tabClasificacion;
    private TextView     tabPartidos;
    private LinearLayout contenedorFilas;
    private LinearLayout contenedorPartidos;
    private Button       btnSelectorJornada;

    // ---- Estado ----
    private int     idLiga;
    private int     jornadaSeleccionada = 1;
    private int     totalJornadas       = 38;
    private boolean clasificacionCargada = false;

    // ---- API ----
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liga_detalle);

        idLiga = getIntent().getIntExtra("ID_LIGA_SELECCIONADA", 140);

        // Ajustar número total de jornadas según la liga
        if (idLiga == 2 || idLiga == 3) {
            totalJornadas = 8;
        } else {
            totalJornadas = 38;
        }

        bindViews();
        setupListeners();

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 1. Cargar clasificación (panel visible por defecto)
        cargarClasificacion();

        // 2. Obtener jornada actual del servidor y luego cargar partidos
        cargarJornadaActual();
    }

    // ─────────────────────────────────────────────
    //  Binding y listeners
    // ─────────────────────────────────────────────

    private void bindViews() {
        panelClasificacion = findViewById(R.id.panelClasificacion);
        panelPartidos      = findViewById(R.id.panelPartidos);
        tabClasificacion   = findViewById(R.id.tabClasificacion);
        tabPartidos        = findViewById(R.id.tabPartidos);
        contenedorFilas    = findViewById(R.id.contenedorFilas);
        contenedorPartidos = findViewById(R.id.contenedorPartidos);
        btnSelectorJornada = findViewById(R.id.btnSelectorJornada);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        tabClasificacion.setOnClickListener(v -> mostrarPanelClasificacion());
        tabPartidos.setOnClickListener(v -> mostrarPanelPartidos());
        btnSelectorJornada.setOnClickListener(v -> abrirSelectorJornada());
    }

    // ─────────────────────────────────────────────
    //  Cambio de tabs
    // ─────────────────────────────────────────────

    private void mostrarPanelClasificacion() {
        panelClasificacion.setVisibility(View.VISIBLE);
        panelPartidos.setVisibility(View.GONE);
        tabClasificacion.setTextColor(0xFFFFFFFF);
        tabClasificacion.setBackgroundResource(R.drawable.tab_selected_indicator);
        tabPartidos.setTextColor(0xFF999999);
        tabPartidos.setBackgroundColor(0x00000000);
    }

    private void mostrarPanelPartidos() {
        panelClasificacion.setVisibility(View.GONE);
        panelPartidos.setVisibility(View.VISIBLE);
        tabPartidos.setTextColor(0xFFFFFFFF);
        tabPartidos.setBackgroundResource(R.drawable.tab_selected_indicator);
        tabClasificacion.setTextColor(0xFF999999);
        tabClasificacion.setBackgroundColor(0x00000000);
    }

    // ─────────────────────────────────────────────
    //  Jornada actual
    // ─────────────────────────────────────────────

    private void cargarJornadaActual() {
        // Actualizar botón con el valor actual mientras carga
        actualizarBotonJornada();

        apiService.getJornadaActual(idLiga).enqueue(new Callback<JornadaActual>() {
            @Override
            public void onResponse(Call<JornadaActual> call, Response<JornadaActual> response) {
                if (response.isSuccessful() && response.body() != null && response.body().jornada > 0) {
                    jornadaSeleccionada = response.body().jornada;
                    Log.d(TAG, "Jornada actual recibida: " + jornadaSeleccionada);
                } else {
                    Log.w(TAG, "Respuesta jornada-actual vacía o inválida, usando jornada " + jornadaSeleccionada);
                }
                actualizarBotonJornada();
                cargarPartidosJornada(jornadaSeleccionada);
            }

            @Override
            public void onFailure(Call<JornadaActual> call, Throwable t) {
                // Si falla la petición de jornada, cargamos igualmente con jornadaSeleccionada
                Log.e(TAG, "Error obteniendo jornada actual: " + t.getMessage());
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

    // ─────────────────────────────────────────────
    //  Clasificación
    // ─────────────────────────────────────────────

    private void cargarClasificacion() {
        if (clasificacionCargada) return;

        apiService.getClasificacion(idLiga).enqueue(new Callback<List<Clasificacion>>() {
            @Override
            public void onResponse(Call<List<Clasificacion>> call,
                                   Response<List<Clasificacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clasificacionCargada = true;
                    mostrarClasificacion(response.body());
                } else {
                    Log.e(TAG, "Error clasificación HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Clasificacion>> call, Throwable t) {
                Log.e(TAG, "Fallo clasificación: " + t.getMessage());
            }
        });
    }

    private void mostrarClasificacion(List<Clasificacion> equipos) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < equipos.size(); i++) {
            Clasificacion eq = equipos.get(i);
            View fila = inflater.inflate(R.layout.item_clasificacion, contenedorFilas, false);

            fila.findViewById(R.id.filaEquipo)
                    .setBackgroundColor(i % 2 == 0 ? 0xFF0A0A0A : 0xFF111111);

            TextView txtPuesto = fila.findViewById(R.id.txtPuesto);
            txtPuesto.setText(String.valueOf(eq.puesto));
            if      (eq.puesto <= 4)  txtPuesto.setTextColor(0xFF4FC3F7);
            else if (eq.puesto <= 6)  txtPuesto.setTextColor(0xFFFF9800);
            else if (eq.puesto >= 18) txtPuesto.setTextColor(0xFFE53935);
            else                      txtPuesto.setTextColor(0xFFFFFFFF);

            ImageView imgEscudo = fila.findViewById(R.id.imgEscudo);
            if (eq.escudo != null && !eq.escudo.isEmpty()) {
                Glide.with(this).load(eq.escudo)
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

    // ─────────────────────────────────────────────
    //  Partidos de la jornada
    // ─────────────────────────────────────────────

    private void cargarPartidosJornada(int jornada) {
        // Limpiar la lista anterior
        contenedorPartidos.removeAllViews();

        // Mostrar "Cargando..." mientras espera
        mostrarMensaje("Cargando partidos...", 0xFFAAAAAA);

        Log.d(TAG, "Cargando partidos: liga=" + idLiga + " jornada=" + jornada);

        apiService.getPartidosJornada(idLiga, jornada).enqueue(new Callback<List<Partido>>() {
            @Override
            public void onResponse(Call<List<Partido>> call, Response<List<Partido>> response) {
                contenedorPartidos.removeAllViews(); // quitar el "Cargando..."

                if (response.isSuccessful() && response.body() != null) {
                    List<Partido> partidos = response.body();
                    Log.d(TAG, "Partidos recibidos: " + partidos.size());

                    if (partidos.isEmpty()) {
                        mostrarMensaje("No hay partidos disponibles para esta jornada", 0xFF999999);
                    } else {
                        mostrarPartidos(partidos);
                    }
                } else {
                    Log.e(TAG, "Error HTTP partidos: " + response.code());
                    mostrarMensaje("No se pudieron cargar los partidos (código " + response.code() + ")", 0xFFFF6B6B);
                }
            }

            @Override
            public void onFailure(Call<List<Partido>> call, Throwable t) {
                Log.e(TAG, "Fallo de red partidos: " + t.getMessage());
                contenedorPartidos.removeAllViews();
                mostrarMensaje("Sin conexión con el servidor.\nAsegúrate de que el backend está arrancado.", 0xFFFF6B6B);
            }
        });
    }

    private void abrirPartidoDetalle(Partido p) {
        boolean esFuturo = "NS".equals(p.estado) || "TBD".equals(p.estado);
        android.content.Intent intent;
        if (esFuturo) {
            intent = new android.content.Intent(this, PrediccionActivity.class);
        } else {
            intent = new android.content.Intent(this, EstadisticasPartidoActivity.class);
            intent.putExtra("GOLES_LOCAL",     p.golesLocal     != null ? p.golesLocal     : 0);
            intent.putExtra("GOLES_VISITANTE", p.golesVisitante != null ? p.golesVisitante : 0);
        }
        intent.putExtra("FIXTURE_ID",         p.fixtureId);
        intent.putExtra("LEAGUE_ID",          idLiga);
        intent.putExtra("LOCAL_TEAM_ID",      p.localTeamId);
        intent.putExtra("VISITANTE_TEAM_ID",  p.visitanteTeamId);
        intent.putExtra("LOCAL_NOMBRE",       p.localNombre);
        intent.putExtra("LOCAL_ESCUDO",       p.localEscudo);
        intent.putExtra("VISITANTE_NOMBRE",   p.visitanteNombre);
        intent.putExtra("VISITANTE_ESCUDO",   p.visitanteEscudo);
        startActivity(intent);
    }

    private void abrirEstadisticas(int teamId, String nombre, String escudo) {
        android.content.Intent intent = new android.content.Intent(this, EstadisticasEquipoActivity.class);
        intent.putExtra("TEAM_ID",     teamId);
        intent.putExtra("LEAGUE_ID",   idLiga);
        intent.putExtra("TEAM_NOMBRE", nombre);
        intent.putExtra("TEAM_ESCUDO", escudo);
        startActivity(intent);
    }

    private void mostrarPartidos(List<Partido> partidos) {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Partido p : partidos) {
            View fila = inflater.inflate(R.layout.item_partido, contenedorPartidos, false);

            // Escudos
            ImageView imgLocal     = fila.findViewById(R.id.imgEscudoLocal);
            ImageView imgVisitante = fila.findViewById(R.id.imgEscudoVisitante);

            cargarEscudo(imgLocal,     p.localEscudo);
            cargarEscudo(imgVisitante, p.visitanteEscudo);

            // Click en escudo → abre estadísticas del equipo
            imgLocal.setOnClickListener(v -> abrirEstadisticas(p.localTeamId, p.localNombre, p.localEscudo));
            imgVisitante.setOnClickListener(v -> abrirEstadisticas(p.visitanteTeamId, p.visitanteNombre, p.visitanteEscudo));

            // Click en centro (fecha/hora) → predicción o estadísticas del partido
            View centro = fila.findViewById(R.id.layoutCentroPartido);
            if (centro != null) {
                centro.setOnClickListener(v -> abrirPartidoDetalle(p));
            }

            // Fecha y hora
            ((TextView) fila.findViewById(R.id.txtFecha)).setText(p.fecha);
            ((TextView) fila.findViewById(R.id.txtHora)).setText(p.hora);

            // Resultado (solo si el partido ya empezó)
            boolean tieneResultado = p.golesLocal != null && p.golesVisitante != null;
            View layoutResultado = fila.findViewById(R.id.layoutResultado);

            if (tieneResultado) {
                layoutResultado.setVisibility(View.VISIBLE);
                ((TextView) fila.findViewById(R.id.txtGolesLocal))
                        .setText(String.valueOf(p.golesLocal));
                ((TextView) fila.findViewById(R.id.txtGolesVisitante))
                        .setText(String.valueOf(p.golesVisitante));
                // Si el partido está en curso, poner hora en rojo
                if ("1H".equals(p.estado) || "2H".equals(p.estado) || "HT".equals(p.estado)) {
                    ((TextView) fila.findViewById(R.id.txtHora)).setTextColor(0xFFE53935);
                    ((TextView) fila.findViewById(R.id.txtHora)).setText("EN VIVO");
                }
            } else {
                layoutResultado.setVisibility(View.GONE);
            }

            contenedorPartidos.addView(fila);
        }
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

    // ─────────────────────────────────────────────
    //  Selector de jornada
    // ─────────────────────────────────────────────

    private void abrirSelectorJornada() {
        String[] opciones = new String[totalJornadas];
        for (int i = 0; i < totalJornadas; i++) {
            opciones[i] = "Jornada " + (i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una jornada")
                .setItems(opciones, (dialog, which) -> {
                    jornadaSeleccionada = which + 1;
                    actualizarBotonJornada();
                    cargarPartidosJornada(jornadaSeleccionada);
                })
                .show();
    }
}