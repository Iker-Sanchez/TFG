package com.example.tfg;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasJugadorActivity extends AppCompatActivity {

    private static final String TAG = "EstJugador";

    private int    playerId;
    private int    leagueId;
    private EstadisticasJugador statsActuales; // null hasta que llegue la respuesta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_jugador);

        playerId = getIntent().getIntExtra("PLAYER_ID", 0);
        leagueId = getIntent().getIntExtra("LEAGUE_ID", 140);
        String nombre = getIntent().getStringExtra("PLAYER_NOMBRE");
        String foto   = getIntent().getStringExtra("PLAYER_FOTO");
        String escudo = getIntent().getStringExtra("EQUIPO_ESCUDO");

        // Datos básicos inmediatos
        setText(R.id.txtNombreJugador, nombre != null ? nombre : "Jugador");
        setText(R.id.txtValoracion, "-");

        if (foto != null && !foto.isEmpty()) {
            Glide.with(this).load(foto).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgFotoJugador));
        }
        if (escudo != null && !escudo.isEmpty()) {
            Glide.with(this).load(escudo).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgEscudoEquipo));
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón comparar — deshabilitado hasta que lleguen las stats
        ImageView btnComparar = findViewById(R.id.btnComparar);
        if (btnComparar != null) {
            btnComparar.setOnClickListener(v -> {
                if (statsActuales != null) {
                    abrirComparador();
                }
            });
        }

        Log.d(TAG, "Cargando stats jugador id=" + playerId + " liga=" + leagueId);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getEstadisticasJugador(playerId, leagueId).enqueue(new Callback<EstadisticasJugador>() {
            @Override
            public void onResponse(Call<EstadisticasJugador> call,
                                   Response<EstadisticasJugador> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statsActuales = response.body();
                    mostrarEstadisticas(statsActuales);
                    // Activar el botón comparar visualmente
                    runOnUiThread(() -> {
                        ImageView btn = findViewById(R.id.btnComparar);
                        if (btn != null) btn.setAlpha(1.0f);
                    });
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                    mostrarError("No hay estadísticas disponibles en esta liga");
                }
            }

            @Override
            public void onFailure(Call<EstadisticasJugador> call, Throwable t) {
                Log.e(TAG, "Fallo: " + t.getMessage());
                mostrarError("Error de conexión");
            }
        });
    }

    private void mostrarEstadisticas(EstadisticasJugador e) {
        // ── PERFIL ────────────────────────────────────────────────
        setText(R.id.txtNombreJugador, e.nombre);
        setText(R.id.txtPosicion,      traducirPosicion(e.posicion));
        setText(R.id.txtEdad,          e.edad + " años");
        setText(R.id.txtNacionalidad,  e.nacionalidad);
        setText(R.id.txtValoracion,    e.valoracion != null ? e.valoracion : "-");

        // Colorear valoración
        try {
            float nota = Float.parseFloat(e.valoracion);
            int color;
            if      (nota >= 7.5f) color = 0xFF4CAF50;
            else if (nota >= 6.5f) color = 0xFFFFC107;
            else                   color = 0xFFF44336;
            ((TextView) findViewById(R.id.txtValoracion)).setBackgroundColor(color);
        } catch (Exception ignored) {}

        // Foto y escudo
        if (e.foto != null && !e.foto.isEmpty()) {
            Glide.with(this).load(e.foto).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgFotoJugador));
        }
        if (e.escudoEquipo != null && !e.escudoEquipo.isEmpty()) {
            Glide.with(this).load(e.escudoEquipo).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgEscudoEquipo));
        }

        // ── PARTICIPACIÓN ─────────────────────────────────────────
        setText(R.id.txtPartidos,      String.valueOf(e.partidos));
        setText(R.id.txtTitularidades, String.valueOf(e.titularidades));
        setText(R.id.txtMinutos,       String.valueOf(e.minutos));

        // ── ATAQUE ────────────────────────────────────────────────
        LinearLayout cAtaque = findViewById(R.id.contenedorAtaque);
        addFila(cAtaque, "⚽  Goles",          String.valueOf(e.goles),           0xFF4CAF50);
        addFila(cAtaque, "🎯  Asistencias",     String.valueOf(e.asistencias),     0xFF4FC3F7);
        addFila(cAtaque, "🔫  Tiros totales",   String.valueOf(e.tirosTotales),    0xFFFFFFFF);
        addFila(cAtaque, "🎯  Tiros a puerta",  String.valueOf(e.tirosPuerta),     0xFFFFFFFF);
        if (e.penaltisConvertidos > 0 || e.penaltisFallados > 0) {
            addFila(cAtaque, "⚡  Penaltis",
                    e.penaltisConvertidos + " gol / " + e.penaltisFallados + " fallo",
                    0xFFFF9800);
        }

        // ── PASES ─────────────────────────────────────────────────
        LinearLayout cPases = findViewById(R.id.contenedorPases);
        addFila(cPases, "↗️  Pases totales",    String.valueOf(e.pasesTotales),    0xFFFFFFFF);
        addFila(cPases, "🔑  Pases clave",      String.valueOf(e.pasesClave),      0xFF4FC3F7);
        addFila(cPases, "✅  Precisión pases",  e.precisionPases + "%",            0xFFFFFFFF);

        // ── DUELOS Y REGATES ──────────────────────────────────────
        LinearLayout cDuelos = findViewById(R.id.contenedorDuelos);
        int pctDuelos = e.duelosTotales > 0
                ? Math.round((e.duelosGanados * 100f) / e.duelosTotales) : 0;
        addFila(cDuelos, "💪  Duelos ganados",
                e.duelosGanados + " / " + e.duelosTotales + " (" + pctDuelos + "%)",
                0xFFFFFFFF);
        int pctRegates = e.regatesIntentados > 0
                ? Math.round((e.regatesExitosos * 100f) / e.regatesIntentados) : 0;
        addFila(cDuelos, "⚡  Regates exitosos",
                e.regatesExitosos + " / " + e.regatesIntentados + " (" + pctRegates + "%)",
                0xFF4FC3F7);
        addFila(cDuelos, "🛡️  Intercepciones",   String.valueOf(e.intercepciones),  0xFFFFFFFF);
        addFila(cDuelos, "🦶  Faltas recibidas",  String.valueOf(e.faltasRecibidas), 0xFFFFFFFF);

        // ── DISCIPLINA ────────────────────────────────────────────
        LinearLayout cDisc = findViewById(R.id.contenedorDisciplina);
        addFila(cDisc, "🟨  Amarillas",         String.valueOf(e.tarjetasAmarillas), 0xFFFFC107);
        addFila(cDisc, "🟥  Rojas",             String.valueOf(e.tarjetasRojas),     0xFFF44336);
        addFila(cDisc, "🦶  Faltas cometidas",  String.valueOf(e.faltasCometidas),   0xFFFFFFFF);
    }

    // ─────────────────────────────────────────────
    //  Abrir comparador
    // ─────────────────────────────────────────────

    private void abrirComparador() {
        EstadisticasJugador e = statsActuales;
        Intent intent = new Intent(this, ComparadorActivity.class);
        intent.putExtra("LEAGUE_ID",          leagueId);
        intent.putExtra("J1_NOMBRE",          e.nombre);
        intent.putExtra("J1_FOTO",            e.foto);
        intent.putExtra("J1_ESCUDO",          e.escudoEquipo);
        intent.putExtra("J1_POSICION",        e.posicion);
        intent.putExtra("J1_VALORACION",      e.valoracion);
        intent.putExtra("J1_PARTIDOS",        e.partidos);
        intent.putExtra("J1_TITULARIDADES",   e.titularidades);
        intent.putExtra("J1_MINUTOS",         e.minutos);
        intent.putExtra("J1_GOLES",           e.goles);
        intent.putExtra("J1_ASISTENCIAS",     e.asistencias);
        intent.putExtra("J1_TIROS",           e.tirosTotales);
        intent.putExtra("J1_TIROS_PUERTA",    e.tirosPuerta);
        intent.putExtra("J1_PASES",           e.pasesTotales);
        intent.putExtra("J1_PASES_CLAVE",     e.pasesClave);
        intent.putExtra("J1_PRECISION_PASES", e.precisionPases);
        intent.putExtra("J1_DUELOS",          e.duelosTotales);
        intent.putExtra("J1_DUELOS_GANADOS",  e.duelosGanados);
        intent.putExtra("J1_REGATES",         e.regatesIntentados);
        intent.putExtra("J1_REGATES_OK",      e.regatesExitosos);
        intent.putExtra("J1_INTERCEPCIONES",  e.intercepciones);
        intent.putExtra("J1_FALTAS_COM",      e.faltasCometidas);
        intent.putExtra("J1_FALTAS_REC",      e.faltasRecibidas);
        intent.putExtra("J1_AMARILLAS",       e.tarjetasAmarillas);
        intent.putExtra("J1_ROJAS",           e.tarjetasRojas);
        intent.putExtra("J1_PENALTIS",        e.penaltisConvertidos);
        startActivity(intent);
    }

    // ─────────────────────────────────────────────
    //  Helpers UI
    // ─────────────────────────────────────────────

    private void addFila(LinearLayout parent, String label, String valor, int colorValor) {
        if (parent.getChildCount() > 0) {
            View sep = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.setMargins(0, dpToPx(10), 0, dpToPx(10));
            sep.setLayoutParams(lp);
            sep.setBackgroundColor(0xFF1F1F1F);
            parent.addView(sep);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(0xFFCCCCCC);
        tvLabel.setTextSize(14f);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        fila.addView(tvLabel);

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextColor(colorValor);
        tvValor.setTextSize(15f);
        tvValor.setTypeface(null, Typeface.BOLD);
        tvValor.setGravity(Gravity.END);
        fila.addView(tvValor);

        parent.addView(fila);
    }

    private String traducirPosicion(String pos) {
        if (pos == null) return "Desconocida";
        switch (pos) {
            case "G": case "Goalkeeper": return "Portero";
            case "D": case "Defender":   return "Defensa";
            case "M": case "Midfielder": return "Centrocampista";
            case "F": case "Attacker":   return "Delantero";
            default: return pos;
        }
    }

    private void setText(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) tv.setText(val);
    }

    private void mostrarError(String msg) {
        runOnUiThread(() -> {
            LinearLayout c = findViewById(R.id.contenedorAtaque);
            if (c == null) return;
            c.removeAllViews();
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextColor(0xFFFF6B6B);
            tv.setTextSize(14f);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(24), 0, dpToPx(24));
            c.addView(tv);
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}