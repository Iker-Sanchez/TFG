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

<<<<<<< HEAD
    private static final String TAG = "EstEquipo";

    private static final int ID_LALIGA = 140;
    private static final int ID_PREMIER = 39;

    private int leagueId;
=======
    private static final String TAG = "EstadisticasEquipo";
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_equipo);

<<<<<<< HEAD
        int teamId = getIntent().getIntExtra("TEAM_ID", 0);
        leagueId = getIntent().getIntExtra("LEAGUE_ID", ID_LALIGA);

        String nombre = getIntent().getStringExtra("TEAM_NOMBRE");
        String escudo = getIntent().getStringExtra("TEAM_ESCUDO");

        aplicarHeaderLiga();

        setText(R.id.txtNombreEquipo, nombre != null ? nombre : "Equipo");
        setText(R.id.txtNombreCompleto, nombre != null ? nombre : "");

        ImageView imgEscudo = findViewById(R.id.imgEscudoEquipo);
        if (escudo != null && !escudo.isEmpty()) {
            Glide.with(this)
                    .load(escudo)
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgEscudo);
        }

<<<<<<< HEAD
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

=======
        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Cargar estadísticas de la API
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        api.getEstadisticasEquipo(leagueId, teamId).enqueue(new Callback<EstadisticasEquipo>() {
            @Override
            public void onResponse(Call<EstadisticasEquipo> call,
                                   Response<EstadisticasEquipo> response) {
<<<<<<< HEAD

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                if (response.isSuccessful() && response.body() != null) {
                    mostrarEstadisticas(response.body());
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EstadisticasEquipo> call, Throwable t) {
<<<<<<< HEAD
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }
    private void aplicarHeaderLiga() {

        if (leagueId == ID_PREMIER) {
            cambiarFondo(R.id.headerEstadisticasEquipo, 0xFF37003C);

            cambiarColorTexto(R.id.txtNombreEquipo, 0xFF00FF85);

            setText(R.id.txtLogoLigaEquipo, "PREMIER");
            cambiarFondo(R.id.txtLogoLigaEquipo, 0xFF00FF85);
            cambiarColorTexto(R.id.txtLogoLigaEquipo, 0xFF37003C);

        } else {
            cambiarFondo(R.id.headerEstadisticasEquipo, 0xFFE30613);

            cambiarColorTexto(R.id.txtNombreEquipo, Color.WHITE);

            setText(R.id.txtLogoLigaEquipo, "LALIGA");
            cambiarFondo(R.id.txtLogoLigaEquipo, 0xFF111111);
            cambiarColorTexto(R.id.txtLogoLigaEquipo, Color.WHITE);
        }
    }

    private void mostrarEstadisticas(EstadisticasEquipo e) {

        LinearLayout layoutForma = findViewById(R.id.layoutForma);
        layoutForma.removeAllViews();

        if (e.formaReciente != null && !e.formaReciente.isEmpty()) {

=======
                Log.e(TAG, "Fallo red: " + t.getMessage());
            }
        });
    }

    private void mostrarEstadisticas(EstadisticasEquipo e) {
        // ── FORMA RECIENTE ──────────────────────────────────────
        LinearLayout layoutForma = findViewById(R.id.layoutForma);
        if (e.formaReciente != null && !e.formaReciente.isEmpty()) {
            // Mostrar solo los últimos 5 resultados
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            String forma = e.formaReciente.length() > 5
                    ? e.formaReciente.substring(e.formaReciente.length() - 5)
                    : e.formaReciente;

            for (char c : forma.toCharArray()) {
                TextView tv = new TextView(this);
                tv.setText(String.valueOf(c));
                tv.setTextSize(13f);
<<<<<<< HEAD
=======
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                tv.setGravity(Gravity.CENTER);

                int size = dpToPx(32);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                tv.setLayoutParams(lp);

<<<<<<< HEAD
                if (c == 'W') tv.setBackgroundColor(0xFF00FF85);
                else if (c == 'D') tv.setBackgroundColor(0xFF9E9E9E);
                else if (c == 'L') tv.setBackgroundColor(0xFFFF2882);
                else tv.setBackgroundColor(0xFF333333);

                tv.setTextColor(Color.WHITE);

=======
                // Color según resultado
                if (c == 'W') tv.setBackgroundColor(0xFF4CAF50);      // Verde
                else if (c == 'D') tv.setBackgroundColor(0xFF9E9E9E); // Gris
                else if (c == 'L') tv.setBackgroundColor(0xFFF44336); // Rojo
                else tv.setBackgroundColor(0xFF333333);

>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                layoutForma.addView(tv);
            }
        }

<<<<<<< HEAD
        setText(R.id.txtPJ, String.valueOf(e.partidosJugados));
        setText(R.id.txtV, String.valueOf(e.victorias));
        setText(R.id.txtE, String.valueOf(e.empates));
        setText(R.id.txtD, String.valueOf(e.derrotas));

        setText(R.id.txtGF, String.valueOf(e.golesFavor));
        setText(R.id.txtGC, String.valueOf(e.golesContra));

        setText(R.id.txtDG, (e.diferencia >= 0 ? "+" : "") + e.diferencia);
        setText(R.id.txtPctVictorias, e.porcentajeVictorias + "%");

        setText(R.id.txtVLocal, String.valueOf(e.victoriasLocal));
        setText(R.id.txtELocal, String.valueOf(e.empatesLocal));
        setText(R.id.txtDLocal, String.valueOf(e.derrotasLocal));

        setText(R.id.txtVVisitante, String.valueOf(e.victoriasVisitante));
        setText(R.id.txtEVisitante, String.valueOf(e.empatesVisitante));
        setText(R.id.txtDVisitante, String.valueOf(e.derrotasVisitante));

        setText(R.id.txtMediaGF, String.format("%.1f", e.mediaGolesFavor));
        setText(R.id.txtMediaGC, String.format("%.1f", e.mediaGolesContra));

        setText(R.id.txtAmarillas, String.valueOf(e.tarjetasAmarillas));
        setText(R.id.txtRojas, String.valueOf(e.tarjetasRojas));
        setText(R.id.txtPenaltis, String.valueOf(e.penaltisAFavor));

=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        setText(R.id.txtMaxVictorias, String.valueOf(e.maxRachaVictorias));
        setText(R.id.txtMaxSinPerder, String.valueOf(e.maxRachaSinPerder));
    }

<<<<<<< HEAD
    // 🔧 UTILIDADES

    private void setText(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) tv.setText(val);
    }

    private void cambiarFondo(int id, int color) {
        View v = findViewById(id);
        if (v != null) v.setBackgroundColor(color);
    }

    private void cambiarColorTexto(int id, int color) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setTextColor(color);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
=======
    private void setText(int viewId, String valor) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(valor);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
    }
}