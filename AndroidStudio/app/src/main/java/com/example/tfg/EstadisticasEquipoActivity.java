package com.example.tfg;

import android.graphics.Color;
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

public class EstadisticasEquipoActivity extends AppCompatActivity {

    private static final String TAG = "EstadisticasEquipo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_equipo);

        // Datos recibidos del Intent
        int    teamId  = getIntent().getIntExtra("TEAM_ID", 0);
        int    leagueId = getIntent().getIntExtra("LEAGUE_ID", 140);
        String nombre  = getIntent().getStringExtra("TEAM_NOMBRE");
        String escudo  = getIntent().getStringExtra("TEAM_ESCUDO");

        // Cabecera
        ((TextView)  findViewById(R.id.txtNombreEquipo)).setText(nombre != null ? nombre : "Estadísticas");
        ((TextView)  findViewById(R.id.txtNombreCompleto)).setText(nombre != null ? nombre : "");

        ImageView imgEscudo = findViewById(R.id.imgEscudoEquipo);
        if (escudo != null && !escudo.isEmpty()) {
            Glide.with(this).load(escudo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgEscudo);
        }

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cargar estadísticas de la API
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getEstadisticasEquipo(leagueId, teamId).enqueue(new Callback<EstadisticasEquipo>() {
            @Override
            public void onResponse(Call<EstadisticasEquipo> call,
                                   Response<EstadisticasEquipo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarEstadisticas(response.body());
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EstadisticasEquipo> call, Throwable t) {
                Log.e(TAG, "Fallo red: " + t.getMessage());
            }
        });
    }

    private void mostrarEstadisticas(EstadisticasEquipo e) {
        // ── FORMA RECIENTE ──────────────────────────────────────
        LinearLayout layoutForma = findViewById(R.id.layoutForma);
        if (e.formaReciente != null && !e.formaReciente.isEmpty()) {
            // Mostrar solo los últimos 5 resultados
            String forma = e.formaReciente.length() > 5
                    ? e.formaReciente.substring(e.formaReciente.length() - 5)
                    : e.formaReciente;

            for (char c : forma.toCharArray()) {
                TextView tv = new TextView(this);
                tv.setText(String.valueOf(c));
                tv.setTextSize(13f);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                tv.setGravity(Gravity.CENTER);

                int size = dpToPx(32);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                tv.setLayoutParams(lp);

                // Color según resultado
                if (c == 'W') tv.setBackgroundColor(0xFF4CAF50);      // Verde
                else if (c == 'D') tv.setBackgroundColor(0xFF9E9E9E); // Gris
                else if (c == 'L') tv.setBackgroundColor(0xFFF44336); // Rojo
                else tv.setBackgroundColor(0xFF333333);

                layoutForma.addView(tv);
            }
        }

        // ── RESULTADOS GENERALES ────────────────────────────────
        setText(R.id.txtPJ, String.valueOf(e.partidosJugados));
        setText(R.id.txtV,  String.valueOf(e.victorias));
        setText(R.id.txtE,  String.valueOf(e.empates));
        setText(R.id.txtD,  String.valueOf(e.derrotas));

        String gf = String.valueOf(e.golesFavor);
        String gc = String.valueOf(e.golesContra);
        String dg = e.diferencia >= 0 ? "+" + e.diferencia : String.valueOf(e.diferencia);
        setText(R.id.txtGF, gf);
        setText(R.id.txtGC, gc);
        setText(R.id.txtDG, dg);
        setText(R.id.txtPctVictorias, e.porcentajeVictorias + "%");

        // ── LOCAL / VISITANTE ────────────────────────────────────
        setText(R.id.txtVLocal,      String.valueOf(e.victoriasLocal));
        setText(R.id.txtELocal,      String.valueOf(e.empatesLocal));
        setText(R.id.txtDLocal,      String.valueOf(e.derrotasLocal));
        setText(R.id.txtVVisitante,  String.valueOf(e.victoriasVisitante));
        setText(R.id.txtEVisitante,  String.valueOf(e.empatesVisitante));
        setText(R.id.txtDVisitante,  String.valueOf(e.derrotasVisitante));

        // ── MEDIAS ────────────────────────────────────────────────
        setText(R.id.txtMediaGF, String.format("%.1f", e.mediaGolesFavor));
        setText(R.id.txtMediaGC, String.format("%.1f", e.mediaGolesContra));

        // ── DISCIPLINA ────────────────────────────────────────────
        setText(R.id.txtAmarillas, String.valueOf(e.tarjetasAmarillas));
        setText(R.id.txtRojas,     String.valueOf(e.tarjetasRojas));
        setText(R.id.txtPenaltis,  String.valueOf(e.penaltisAFavor));

        // ── RACHAS ────────────────────────────────────────────────
        setText(R.id.txtMaxVictorias, String.valueOf(e.maxRachaVictorias));
        setText(R.id.txtMaxSinPerder, String.valueOf(e.maxRachaSinPerder));
    }

    private void setText(int viewId, String valor) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(valor);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}