package com.example.tfg;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class PrediccionActivity extends AppCompatActivity {

    private static final String TAG = "PrediccionActivity";

    private int leagueId;
    private int localTeamId;
    private int visitanteTeamId;
    private int fixtureId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediccion);

        fixtureId       = getIntent().getIntExtra("FIXTURE_ID", 0);
        leagueId        = getIntent().getIntExtra("LEAGUE_ID", 140);
        localTeamId     = getIntent().getIntExtra("LOCAL_TEAM_ID", 0);
        visitanteTeamId = getIntent().getIntExtra("VISITANTE_TEAM_ID", 0);
        String localNombre     = getIntent().getStringExtra("LOCAL_NOMBRE");
        String localEscudo     = getIntent().getStringExtra("LOCAL_ESCUDO");
        String visitanteNombre = getIntent().getStringExtra("VISITANTE_NOMBRE");
        String visitanteEscudo = getIntent().getStringExtra("VISITANTE_ESCUDO");

        cargarEscudo(findViewById(R.id.imgEscudoLocal),     localEscudo);
        cargarEscudo(findViewById(R.id.imgEscudoVisitante), visitanteEscudo);
        setText(R.id.txtNombreLocal,          localNombre);
        setText(R.id.txtNombreVisitante,      visitanteNombre);
        setText(R.id.txtGolesLocalLabel,      localNombre);
        setText(R.id.txtGolesVisitanteLabel,  visitanteNombre);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Log.d(TAG, "Cargando predicción para fixtureId=" + fixtureId);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        // Cargar oncenas si tenemos los IDs
        if (localTeamId > 0) cargarOnce(api, localTeamId, localNombre, true);
        if (visitanteTeamId > 0) cargarOnce(api, visitanteTeamId, visitanteNombre, false);

        api.getPrediccion(fixtureId).enqueue(new Callback<PrediccionPartido>() {
            @Override
            public void onResponse(Call<PrediccionPartido> call,
                                   Response<PrediccionPartido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarPrediccion(response.body());
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                    mostrarMensajeError("No hay predicción disponible para este partido");
                }
            }

            @Override
            public void onFailure(Call<PrediccionPartido> call, Throwable t) {
                Log.e(TAG, "Fallo red: " + t.getMessage());
                mostrarMensajeError("Error de conexión");
            }
        });
    }

    private void mostrarPrediccion(PrediccionPartido p) {

        // ── GANADOR ───────────────────────────────────────────────
        TextView txtGanador = findViewById(R.id.txtGanador);
        txtGanador.setText(p.ganadorNombre != null ? p.ganadorNombre : "Sin predicción");
        if (p.ganadorNombre != null && (p.ganadorNombre.toLowerCase().contains("empate")
                || p.ganadorNombre.toLowerCase().contains("draw"))) {
            txtGanador.setTextColor(0xFFFFC107);
        } else {
            txtGanador.setTextColor(0xFF4CAF50);
        }
        setText(R.id.txtConsejo, p.consejo != null ? "💡 " + p.consejo : "");

        // ── PROBABILIDADES ────────────────────────────────────────
        setText(R.id.txtPctLocal,     p.pctLocal     != null ? p.pctLocal     : "?");
        setText(R.id.txtPctEmpate,    p.pctEmpate    != null ? p.pctEmpate    : "?");
        setText(R.id.txtPctVisitante, p.pctVisitante != null ? p.pctVisitante : "?");
        actualizarBarra(p.pctLocal, p.pctEmpate, p.pctVisitante);

        // ── COMPARATIVA (programáticamente) ───────────────────────
        LinearLayout contenedor = findViewById(R.id.contenedorComparativa);
        contenedor.removeAllViews();
        addFilaComparativa(contenedor, p.formaLocal,  "Forma",   p.formaVisitante,  true);
        addFilaComparativa(contenedor, p.ataqueLocal, "Ataque",  p.ataqueVisitante, true);
        addFilaComparativa(contenedor, p.defensaLocal,"Defensa", p.defensaVisitante,true);
        addFilaComparativa(contenedor, p.h2hLocal,    "H2H",     p.h2hVisitante,    true);
        addFilaComparativa(contenedor, p.totalLocal,  "Total",   p.totalVisitante,  true);

        // ── GOLES ESPERADOS ───────────────────────────────────────
        String gL = p.golesEsperadosLocal     != null ? p.golesEsperadosLocal.replace("-","")     : "?";
        String gV = p.golesEsperadosVisitante != null ? p.golesEsperadosVisitante.replace("-","") : "?";
        setText(R.id.txtGolesLocal,     gL.isEmpty() ? "?" : gL);
        setText(R.id.txtGolesVisitante, gV.isEmpty() ? "?" : gV);

        // ── FORMA RECIENTE ────────────────────────────────────────
        pintarForma(R.id.layoutFormaLocal,     p.formaRecienteLocal);
        pintarForma(R.id.layoutFormaVisitante, p.formaRecienteVisitante);

        // ── H2H ───────────────────────────────────────────────────
        pintarH2H(p.h2hResultados);
    }

    // ─────────────────────────────────────────────
    //  Helpers de UI
    // ─────────────────────────────────────────────

    /** Añade una fila de comparativa creada enteramente por código */
    private void addFilaComparativa(LinearLayout parent,
                                    String valLocal, String label, String valVis,
                                    boolean mayorEsMejor) {
        // Separador si no es la primera fila
        if (parent.getChildCount() > 0) {
            View sep = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            lp.setMargins(0, dpToPx(8), 0, dpToPx(8));
            sep.setLayoutParams(lp);
            sep.setBackgroundColor(0xFF1F1F1F);
            parent.addView(sep);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Colores por defecto
        int colorLocal     = 0xFFAAAAAA;
        int colorVisitante = 0xFFAAAAAA;
        try {
            float vL = Float.parseFloat(valLocal  != null ? valLocal.replace("%","")  : "0");
            float vV = Float.parseFloat(valVis    != null ? valVis.replace("%","")    : "0");
            if (mayorEsMejor ? vL > vV : vL < vV) {
                colorLocal     = 0xFF4FC3F7;
            } else if (mayorEsMejor ? vV > vL : vV < vL) {
                colorVisitante = 0xFF4CAF50;
            }
        } catch (Exception ignored) {}

        TextView tvLocal = makeStatText(valLocal != null ? valLocal : "-", colorLocal, 60, Gravity.CENTER);
        TextView tvLabel = makeStatText(label, 0xFFAAAAAA, 0, Gravity.CENTER);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setTextSize(13f);
        TextView tvVis = makeStatText(valVis != null ? valVis : "-", colorVisitante, 60, Gravity.CENTER);

        fila.addView(tvLocal);
        fila.addView(tvLabel);
        fila.addView(tvVis);
        parent.addView(fila);
    }

    private void actualizarBarra(String pLocal, String pEmpate, String pVis) {
        try {
            float l = pLocal  != null ? Float.parseFloat(pLocal.replace("%",""))  : 33f;
            float e = pEmpate != null ? Float.parseFloat(pEmpate.replace("%","")) : 33f;
            float v = pVis    != null ? Float.parseFloat(pVis.replace("%",""))    : 34f;

            setBarraWeight(R.id.barraLocal,     l);
            setBarraWeight(R.id.barraEmpate,    e);
            setBarraWeight(R.id.barraVisitante, v);
        } catch (Exception ex) {
            Log.e(TAG, "Error barra: " + ex.getMessage());
        }
    }

    private void setBarraWeight(int id, float weight) {
        android.view.View v = findViewById(id);
        if (v == null) return;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, weight);
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
            if      (c == 'W') tv.setBackgroundColor(0xFF4CAF50);
            else if (c == 'D') tv.setBackgroundColor(0xFF9E9E9E);
            else if (c == 'L') tv.setBackgroundColor(0xFFF44336);
            else                tv.setBackgroundColor(0xFF333333);
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
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
                sep.setLayoutParams(lp);
                sep.setBackgroundColor(0xFF1F1F1F);
                layout.addView(sep);
            }
        }
    }

    private void cargarOnce(ApiService api, int teamId, String nombre, boolean esLocal) {
        int fieldId  = esLocal ? R.id.fieldViewLocal    : R.id.fieldViewVisitante;
        int tituloId = esLocal ? R.id.txtTituloOnceLocal : R.id.txtTituloOnceVisitante;

        // Actualizar título con nombre del equipo
        runOnUiThread(() -> {
            TextView titulo = findViewById(tituloId);
            if (titulo != null) titulo.setText("ONCE PROBABLE — " + nombre.toUpperCase());
        });

        final boolean esLocalFinal = esLocal;
        api.getOnceProbable(leagueId, teamId, fixtureId).enqueue(new Callback<OnceProbable>() {
            @Override
            public void onResponse(Call<OnceProbable> call, Response<OnceProbable> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String escudo = esLocalFinal
                            ? getIntent().getStringExtra("LOCAL_ESCUDO")
                            : getIntent().getStringExtra("VISITANTE_ESCUDO");
                    mostrarOnceEnCampo(fieldId, response.body(), escudo != null ? escudo : "");
                }
            }
            @Override
            public void onFailure(Call<OnceProbable> call, Throwable t) {
                Log.e(TAG, "Fallo once " + teamId + ": " + t.getMessage());
            }
        });
    }

    private void mostrarOnceEnCampo(int fieldId, OnceProbable once, String escudoEquipo) {
        runOnUiThread(() -> {
            FieldView fieldView = findViewById(fieldId);
            if (fieldView == null || once == null) return;

            // Convertir modelo OnceProbable.Jugador → FieldView.PlayerSlot
            List<FieldView.PlayerSlot> slots = new java.util.ArrayList<>();
            if (once.jugadores != null) {
                for (OnceProbable.Jugador j : once.jugadores) {
                    slots.add(new FieldView.PlayerSlot(j.nombre, j.posicion, j.id));
                }
            }
            fieldView.setLineup(once.formacion, slots);

            // Click en jugador → pantalla de estadísticas del jugador
            fieldView.setOnPlayerClickListener(slot -> {
                if (slot.playerId <= 0) return;
                // Buscar la foto del jugador
                String foto = "";
                if (once.jugadores != null) {
                    for (OnceProbable.Jugador j : once.jugadores) {
                        if (j.id == slot.playerId) { foto = j.foto != null ? j.foto : ""; break; }
                    }
                }
                android.content.Intent intent = new android.content.Intent(
                        this, EstadisticasJugadorActivity.class);
                intent.putExtra("PLAYER_ID",    slot.playerId);
                intent.putExtra("LEAGUE_ID",    leagueId);
                intent.putExtra("PLAYER_NOMBRE", slot.nombre);
                intent.putExtra("PLAYER_FOTO",  foto);
                intent.putExtra("EQUIPO_ESCUDO", escudoEquipo);
                startActivity(intent);
            });
        });
    }


    private void mostrarMensajeError(String msg) {
        runOnUiThread(() -> {
            LinearLayout layout = findViewById(R.id.layoutH2H);
            if (layout == null) return;
            layout.removeAllViews();
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextColor(0xFFFF6B6B);
            tv.setTextSize(14f);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(16), 0, dpToPx(16));
            layout.addView(tv);
        });
    }

    private TextView makeStatText(String texto, int color, int widthDp, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(color);
        tv.setTextSize(15f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(gravity);
        if (widthDp > 0) {
            tv.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(widthDp),
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        return tv;
    }

    private void setText(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) tv.setText(val);
    }

    private void cargarEscudo(ImageView iv, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}