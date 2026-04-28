package com.example.tfg;

import android.graphics.Color;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremierPrediccionActivity extends AppCompatActivity {

    private static final String TAG = "PremierPrediccion";

    private static final int ID_PREMIER = 39;
    private static final int COLOR_MORADO_PREMIER = 0xFF37003C;
    private static final int COLOR_VERDE_PREMIER = 0xFF00FF85;
    private static final int COLOR_ROSA_PREMIER = 0xFFFF2882;

    private int leagueId;
    private int localTeamId;
    private int visitanteTeamId;
    private int fixtureId;

    private String localNombre;
    private String localEscudo;
    private String visitanteNombre;
    private String visitanteEscudo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediccion);

        fixtureId = getIntent().getIntExtra("FIXTURE_ID", 0);
        leagueId = getIntent().getIntExtra("LEAGUE_ID", ID_PREMIER);
        localTeamId = getIntent().getIntExtra("LOCAL_TEAM_ID", 0);
        visitanteTeamId = getIntent().getIntExtra("VISITANTE_TEAM_ID", 0);

        localNombre = getIntent().getStringExtra("LOCAL_NOMBRE");
        localEscudo = getIntent().getStringExtra("LOCAL_ESCUDO");
        visitanteNombre = getIntent().getStringExtra("VISITANTE_NOMBRE");
        visitanteEscudo = getIntent().getStringExtra("VISITANTE_ESCUDO");

        aplicarTemaPremier();

        ImageView imgLocal = findViewById(R.id.imgEscudoLocal);
        ImageView imgVisitante = findViewById(R.id.imgEscudoVisitante);

        cargarEscudo(imgLocal, localEscudo);
        cargarEscudo(imgVisitante, visitanteEscudo);

        setText(R.id.txtNombreLocal, localNombre);
        setText(R.id.txtNombreVisitante, visitanteNombre);
        setText(R.id.txtGolesLocalLabel, localNombre);
        setText(R.id.txtGolesVisitanteLabel, visitanteNombre);

        imgLocal.setOnClickListener(v ->
                abrirEstadisticasEquipo(localTeamId, localNombre, localEscudo)
        );

        imgVisitante.setOnClickListener(v ->
                abrirEstadisticasEquipo(visitanteTeamId, visitanteNombre, visitanteEscudo)
        );

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        if (localTeamId > 0) {
            cargarOnce(api, localTeamId, localNombre, true);
        }

        if (visitanteTeamId > 0) {
            cargarOnce(api, visitanteTeamId, visitanteNombre, false);
        }

        api.getPrediccion(fixtureId).enqueue(new Callback<PrediccionPartido>() {
            @Override
            public void onResponse(Call<PrediccionPartido> call,
                                   Response<PrediccionPartido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarPrediccion(response.body());
                } else {
                    mostrarMensajeError("No hay predicción disponible para este partido");
                    Log.e(TAG, "Error HTTP predicción Premier: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PrediccionPartido> call, Throwable t) {
                mostrarMensajeError("Error de conexión");
                Log.e(TAG, "Fallo predicción Premier: " + t.getMessage());
            }
        });
    }

    private void aplicarTemaPremier() {
        getWindow().getDecorView().setBackgroundColor(0xFF050006);

        cambiarColorTexto(R.id.txtNombreLocal, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtNombreVisitante, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtGanador, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtConsejo, Color.WHITE);
        cambiarColorTexto(R.id.txtPctLocal, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtPctEmpate, COLOR_ROSA_PREMIER);
        cambiarColorTexto(R.id.txtPctVisitante, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtGolesLocal, COLOR_VERDE_PREMIER);
        cambiarColorTexto(R.id.txtGolesVisitante, COLOR_VERDE_PREMIER);
    }

    private void mostrarPrediccion(PrediccionPartido p) {
        TextView txtGanador = findViewById(R.id.txtGanador);

        txtGanador.setText(p.ganadorNombre != null ? p.ganadorNombre : "Sin predicción");

        if (p.ganadorNombre != null &&
                (p.ganadorNombre.toLowerCase().contains("empate")
                        || p.ganadorNombre.toLowerCase().contains("draw"))) {
            txtGanador.setTextColor(COLOR_ROSA_PREMIER);
        } else {
            txtGanador.setTextColor(COLOR_VERDE_PREMIER);
        }

        setText(R.id.txtConsejo, p.consejo != null ? "💡 " + p.consejo : "");

        setText(R.id.txtPctLocal, p.pctLocal != null ? p.pctLocal : "?");
        setText(R.id.txtPctEmpate, p.pctEmpate != null ? p.pctEmpate : "?");
        setText(R.id.txtPctVisitante, p.pctVisitante != null ? p.pctVisitante : "?");

        actualizarBarra(p.pctLocal, p.pctEmpate, p.pctVisitante);

        LinearLayout contenedor = findViewById(R.id.contenedorComparativa);
        contenedor.removeAllViews();

        addFilaComparativa(contenedor, p.formaLocal, "Forma", p.formaVisitante, true);
        addFilaComparativa(contenedor, p.ataqueLocal, "Ataque", p.ataqueVisitante, true);
        addFilaComparativa(contenedor, p.defensaLocal, "Defensa", p.defensaVisitante, true);
        addFilaComparativa(contenedor, p.h2hLocal, "H2H", p.h2hVisitante, true);
        addFilaComparativa(contenedor, p.totalLocal, "Total", p.totalVisitante, true);

        String golesLocal = p.golesEsperadosLocal != null
                ? p.golesEsperadosLocal.replace("-", "")
                : "?";

        String golesVisitante = p.golesEsperadosVisitante != null
                ? p.golesEsperadosVisitante.replace("-", "")
                : "?";

        setText(R.id.txtGolesLocal, golesLocal.isEmpty() ? "?" : golesLocal);
        setText(R.id.txtGolesVisitante, golesVisitante.isEmpty() ? "?" : golesVisitante);

        pintarForma(R.id.layoutFormaLocal, p.formaRecienteLocal);
        pintarForma(R.id.layoutFormaVisitante, p.formaRecienteVisitante);

        pintarH2H(p.h2hResultados);
        aplicarTemaPremier();
    }

    private void cargarOnce(ApiService api, int teamId, String nombre, boolean esLocal) {
        int fieldId = esLocal ? R.id.fieldViewLocal : R.id.fieldViewVisitante;
        int tituloId = esLocal ? R.id.txtTituloOnceLocal : R.id.txtTituloOnceVisitante;

        runOnUiThread(() -> {
            TextView titulo = findViewById(tituloId);
            if (titulo != null) {
                titulo.setText("ONCE PROBABLE — " + nombre.toUpperCase());
                titulo.setTextColor(COLOR_VERDE_PREMIER);
            }
        });

        api.getOnceProbable(leagueId, teamId, fixtureId).enqueue(new Callback<OnceProbable>() {
            @Override
            public void onResponse(Call<OnceProbable> call, Response<OnceProbable> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String escudoEquipo = esLocal ? localEscudo : visitanteEscudo;
                    mostrarOnceEnCampo(fieldId, response.body(), escudoEquipo);
                }
            }

            @Override
            public void onFailure(Call<OnceProbable> call, Throwable t) {
                Log.e(TAG, "Fallo once Premier " + teamId + ": " + t.getMessage());
            }
        });
    }

    private void mostrarOnceEnCampo(int fieldId, OnceProbable once, String escudoEquipo) {
        runOnUiThread(() -> {
            FieldView fieldView = findViewById(fieldId);
            if (fieldView == null || once == null) return;

            List<FieldView.PlayerSlot> slots = new java.util.ArrayList<>();

            if (once.jugadores != null) {
                for (OnceProbable.Jugador j : once.jugadores) {
                    slots.add(new FieldView.PlayerSlot(j.nombre, j.posicion, j.id));
                }
            }

            fieldView.setLineup(once.formacion, slots);

            fieldView.setOnPlayerClickListener(slot -> {
                if (slot.playerId <= 0) return;

                String foto = "";

                if (once.jugadores != null) {
                    for (OnceProbable.Jugador j : once.jugadores) {
                        if (j.id == slot.playerId) {
                            foto = j.foto != null ? j.foto : "";
                            break;
                        }
                    }
                }

                android.content.Intent intent = new android.content.Intent(
                        this,
                        EstadisticasJugadorActivity.class
                );

                intent.putExtra("PLAYER_ID", slot.playerId);
                intent.putExtra("LEAGUE_ID", leagueId);
                intent.putExtra("PLAYER_NOMBRE", slot.nombre);
                intent.putExtra("PLAYER_FOTO", foto);
                intent.putExtra("EQUIPO_ESCUDO", escudoEquipo);

                startActivity(intent);
            });
        });
    }

    private void abrirEstadisticasEquipo(int teamId, String nombre, String escudo) {
        if (teamId <= 0) return;

        android.content.Intent intent = new android.content.Intent(
                this,
                EstadisticasEquipoActivity.class
        );

        intent.putExtra("TEAM_ID", teamId);
        intent.putExtra("LEAGUE_ID", leagueId);
        intent.putExtra("TEAM_NOMBRE", nombre);
        intent.putExtra("TEAM_ESCUDO", escudo);

        startActivity(intent);
    }

    private void addFilaComparativa(LinearLayout parent,
                                    String valLocal,
                                    String label,
                                    String valVisitante,
                                    boolean mayorEsMejor) {
        if (parent.getChildCount() > 0) {
            View sep = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
            );
            lp.setMargins(0, dpToPx(8), 0, dpToPx(8));
            sep.setLayoutParams(lp);
            sep.setBackgroundColor(0xFF37003C);
            parent.addView(sep);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int colorLocal = 0xFFAAAAAA;
        int colorVisitante = 0xFFAAAAAA;

        try {
            float vLocal = Float.parseFloat(valLocal != null ? valLocal.replace("%", "") : "0");
            float vVisitante = Float.parseFloat(valVisitante != null ? valVisitante.replace("%", "") : "0");

            if (mayorEsMejor ? vLocal > vVisitante : vLocal < vVisitante) {
                colorLocal = COLOR_VERDE_PREMIER;
            } else if (mayorEsMejor ? vVisitante > vLocal : vVisitante < vLocal) {
                colorVisitante = COLOR_VERDE_PREMIER;
            }
        } catch (Exception ignored) {}

        TextView tvLocal = makeStatText(valLocal != null ? valLocal : "-", colorLocal, 60, Gravity.CENTER);
        TextView tvLabel = makeStatText(label, 0xFFCCCCCC, 0, Gravity.CENTER);
        TextView tvVisitante = makeStatText(valVisitante != null ? valVisitante : "-", colorVisitante, 60, Gravity.CENTER);

        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));
        tvLabel.setTextSize(13f);

        fila.addView(tvLocal);
        fila.addView(tvLabel);
        fila.addView(tvVisitante);

        parent.addView(fila);
    }

    private void actualizarBarra(String pLocal, String pEmpate, String pVisitante) {
        try {
            float local = pLocal != null ? Float.parseFloat(pLocal.replace("%", "")) : 33f;
            float empate = pEmpate != null ? Float.parseFloat(pEmpate.replace("%", "")) : 33f;
            float visitante = pVisitante != null ? Float.parseFloat(pVisitante.replace("%", "")) : 34f;

            setBarraWeight(R.id.barraLocal, local);
            setBarraWeight(R.id.barraEmpate, empate);
            setBarraWeight(R.id.barraVisitante, visitante);

            View barraLocal = findViewById(R.id.barraLocal);
            View barraEmpate = findViewById(R.id.barraEmpate);
            View barraVisitante = findViewById(R.id.barraVisitante);

            if (barraLocal != null) barraLocal.setBackgroundColor(COLOR_VERDE_PREMIER);
            if (barraEmpate != null) barraEmpate.setBackgroundColor(COLOR_ROSA_PREMIER);
            if (barraVisitante != null) barraVisitante.setBackgroundColor(COLOR_MORADO_PREMIER);

        } catch (Exception e) {
            Log.e(TAG, "Error barra Premier: " + e.getMessage());
        }
    }

    private void setBarraWeight(int id, float weight) {
        View v = findViewById(id);
        if (v == null) return;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
        );

        v.setLayoutParams(lp);
    }

    private void pintarForma(int layoutId, String forma) {
        LinearLayout layout = findViewById(layoutId);
        if (layout == null) return;

        layout.removeAllViews();

        if (forma == null || forma.isEmpty()) return;

        String f = forma.length() > 5 ? forma.substring(forma.length() - 5) : forma;

        for (char c : f.toCharArray()) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(c));
            tv.setTextSize(12f);
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);

            int size = dpToPx(28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(dpToPx(2), 0, dpToPx(2), 0);
            tv.setLayoutParams(lp);

            if (c == 'W') {
                tv.setBackgroundColor(COLOR_VERDE_PREMIER);
                tv.setTextColor(COLOR_MORADO_PREMIER);
            } else if (c == 'D') {
                tv.setBackgroundColor(0xFF9E9E9E);
            } else if (c == 'L') {
                tv.setBackgroundColor(COLOR_ROSA_PREMIER);
            } else {
                tv.setBackgroundColor(0xFF333333);
            }

            layout.addView(tv);
        }
    }

    private void pintarH2H(String[] resultados) {
        LinearLayout layout = findViewById(R.id.layoutH2H);
        if (layout == null) return;

        layout.removeAllViews();

        if (resultados == null || resultados.length == 0) {
            TextView tv = new TextView(this);
            tv.setText("Sin historial disponible");
            tv.setTextColor(0xFF999999);
            tv.setTextSize(13f);
            tv.setGravity(Gravity.CENTER);
            layout.addView(tv);
            return;
        }

        for (int i = 0; i < resultados.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(resultados[i]);
            tv.setTextColor(0xFFCCCCCC);
            tv.setTextSize(13f);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(8), 0, dpToPx(8));

            layout.addView(tv);

            if (i < resultados.length - 1) {
                View sep = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );
                sep.setLayoutParams(lp);
                sep.setBackgroundColor(COLOR_MORADO_PREMIER);
                layout.addView(sep);
            }
        }
    }

    private TextView makeStatText(String texto, int color, int widthDp, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(color);
        tv.setTextSize(15f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(gravity);

        if (widthDp > 0) {
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(widthDp),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        }

        return tv;
    }

    private void mostrarMensajeError(String msg) {
        runOnUiThread(() -> {
            LinearLayout layout = findViewById(R.id.layoutH2H);
            if (layout == null) return;

            layout.removeAllViews();

            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextColor(COLOR_ROSA_PREMIER);
            tv.setTextSize(14f);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(16), 0, dpToPx(16));

            layout.addView(tv);
        });
    }

    private void cargarEscudo(ImageView iv, String url) {
        if (iv != null && url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv);
        }
    }

    private void setText(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) {
            tv.setText(val);
        }
    }

    private void cambiarColorTexto(int id, int color) {
        TextView tv = findViewById(id);
        if (tv != null) {
            tv.setTextColor(color);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}