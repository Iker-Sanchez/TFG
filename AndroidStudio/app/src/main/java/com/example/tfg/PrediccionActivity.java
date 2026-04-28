package com.example.tfg;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
<<<<<<< HEAD
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
=======
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

<<<<<<< HEAD
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
=======
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f

public class PrediccionActivity extends AppCompatActivity {

    private static final String TAG = "PrediccionActivity";

<<<<<<< HEAD
    private static final int ID_LALIGA = 140;
    private static final int ID_PREMIER = 39;

    private static final int LALIGA_ROJO = 0xFFE30613;
    private static final int LALIGA_AMARILLO = 0xFFFFC400;
    private static final int LALIGA_FONDO = 0xFF000000;
    private static final int LALIGA_CARD = 0xFF111111;

    private static final int PREMIER_MORADO = 0xFF37003C;
    private static final int PREMIER_VERDE = 0xFF00FF85;
    private static final int PREMIER_ROSA = 0xFFFF2882;
    private static final int PREMIER_FONDO = 0xFF050006;
    private static final int PREMIER_CARD = 0xFF120016;

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
    private int leagueId;
    private int localTeamId;
    private int visitanteTeamId;
    private int fixtureId;

<<<<<<< HEAD
    private int colorPrincipal;
    private int colorSecundario;
    private int colorTercero;
    private int colorFondo;
    private int colorCard;

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediccion);

<<<<<<< HEAD
        fixtureId = getIntent().getIntExtra("FIXTURE_ID", 0);
        leagueId = getIntent().getIntExtra("LEAGUE_ID", ID_LALIGA);
        localTeamId = getIntent().getIntExtra("LOCAL_TEAM_ID", 0);
        visitanteTeamId = getIntent().getIntExtra("VISITANTE_TEAM_ID", 0);

        String localNombre = getIntent().getStringExtra("LOCAL_NOMBRE");
        String localEscudo = getIntent().getStringExtra("LOCAL_ESCUDO");
        String visitanteNombre = getIntent().getStringExtra("VISITANTE_NOMBRE");
        String visitanteEscudo = getIntent().getStringExtra("VISITANTE_ESCUDO");

        configurarTemaLiga();
        aplicarTemaLiga();

        cargarEscudo(findViewById(R.id.imgEscudoLocal), localEscudo);
        cargarEscudo(findViewById(R.id.imgEscudoVisitante), visitanteEscudo);

        setText(R.id.txtNombreLocal, localNombre);
        setText(R.id.txtNombreVisitante, visitanteNombre);
        setText(R.id.txtGolesLocalLabel, localNombre);
        setText(R.id.txtGolesVisitanteLabel, visitanteNombre);

        ImageView imgLocal = findViewById(R.id.imgEscudoLocal);
        ImageView imgVisitante = findViewById(R.id.imgEscudoVisitante);

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
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f

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

<<<<<<< HEAD
    private void configurarTemaLiga() {
        if (leagueId == ID_PREMIER) {
            colorPrincipal = PREMIER_VERDE;
            colorSecundario = PREMIER_ROSA;
            colorTercero = PREMIER_MORADO;
            colorFondo = PREMIER_FONDO;
            colorCard = PREMIER_CARD;
        } else {
            colorPrincipal = LALIGA_ROJO;
            colorSecundario = LALIGA_AMARILLO;
            colorTercero = 0xFF111111;
            colorFondo = LALIGA_FONDO;
            colorCard = LALIGA_CARD;
        }
    }

    private void aplicarTemaLiga() {
        cambiarFondo(R.id.rootPrediccion, colorFondo);
        cambiarFondo(R.id.scrollPrediccion, colorFondo);
        cambiarFondo(R.id.contenidoPrediccion, colorFondo);

        if (leagueId == ID_PREMIER) {
            cambiarFondo(R.id.headerPrediccion, PREMIER_MORADO);
            cambiarColorTexto(R.id.txtTituloPrediccion, PREMIER_VERDE);
            setText(R.id.txtLogoLigaPrediccion, "PREMIER");
            cambiarColorTexto(R.id.txtLogoLigaPrediccion, PREMIER_MORADO);
            cambiarFondo(R.id.txtLogoLigaPrediccion, PREMIER_VERDE);
            cambiarFondo(R.id.headerEquiposPrediccion, 0xFF120016);
            cambiarFondo(R.id.separadorHeaderPrediccion, PREMIER_VERDE);
        } else {
            cambiarFondo(R.id.headerPrediccion, LALIGA_ROJO);
            cambiarColorTexto(R.id.txtTituloPrediccion, Color.WHITE);
            setText(R.id.txtLogoLigaPrediccion, "LALIGA");
            cambiarColorTexto(R.id.txtLogoLigaPrediccion, Color.WHITE);
            cambiarFondo(R.id.txtLogoLigaPrediccion, 0xFF111111);
            cambiarFondo(R.id.headerEquiposPrediccion, 0xFF111111);
            cambiarFondo(R.id.separadorHeaderPrediccion, 0xFF333333);
        }

        cambiarColorTexto(R.id.txtNombreLocal, colorPrincipal);
        cambiarColorTexto(R.id.txtNombreVisitante, colorPrincipal);
        cambiarColorTexto(R.id.txtVsPrediccion, 0xFF888888);

        cambiarFondo(R.id.cardGanador, colorCard);
        cambiarFondo(R.id.cardProbabilidades, colorCard);
        cambiarFondo(R.id.contenedorComparativa, colorCard);
        cambiarFondo(R.id.cardGolesEsperados, colorCard);
        cambiarFondo(R.id.cardFormaReciente, colorCard);
        cambiarFondo(R.id.layoutH2H, colorCard);

        cambiarColorTexto(R.id.txtGanador, colorPrincipal);
        cambiarColorTexto(R.id.txtConsejo, Color.WHITE);

        cambiarColorTexto(R.id.txtPctLocal, colorPrincipal);
        cambiarColorTexto(R.id.txtPctEmpate, colorSecundario);
        cambiarColorTexto(R.id.txtPctVisitante, colorPrincipal);

        cambiarColorTexto(R.id.txtGolesLocal, colorPrincipal);
        cambiarColorTexto(R.id.txtGolesVisitante, colorPrincipal);

        cambiarFondo(R.id.barraLocal, colorPrincipal);
        cambiarFondo(R.id.barraEmpate, colorSecundario);
        cambiarFondo(R.id.barraVisitante, colorTercero);

        cambiarColorTexto(R.id.txtTituloOnceLocal, colorPrincipal);
        cambiarColorTexto(R.id.txtTituloOnceVisitante, colorPrincipal);
    }

    private void mostrarPrediccion(PrediccionPartido p) {
        TextView txtGanador = findViewById(R.id.txtGanador);
        txtGanador.setText(p.ganadorNombre != null ? p.ganadorNombre : "Sin predicción");

        if (p.ganadorNombre != null &&
                (p.ganadorNombre.toLowerCase().contains("empate")
                        || p.ganadorNombre.toLowerCase().contains("draw"))) {
            txtGanador.setTextColor(colorSecundario);
        } else {
            txtGanador.setTextColor(colorPrincipal);
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

        String gL = p.golesEsperadosLocal != null ? p.golesEsperadosLocal.replace("-", "") : "?";
        String gV = p.golesEsperadosVisitante != null ? p.golesEsperadosVisitante.replace("-", "") : "?";

        setText(R.id.txtGolesLocal, gL.isEmpty() ? "?" : gL);
        setText(R.id.txtGolesVisitante, gV.isEmpty() ? "?" : gV);

        pintarForma(R.id.layoutFormaLocal, p.formaRecienteLocal);
        pintarForma(R.id.layoutFormaVisitante, p.formaRecienteVisitante);
        pintarH2H(p.h2hResultados);

        aplicarTemaLiga();
    }

    private void addFilaComparativa(LinearLayout parent,
                                    String valLocal,
                                    String label,
                                    String valVis,
                                    boolean mayorEsMejor) {
        if (parent.getChildCount() > 0) {
            View sep = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
            );
            lp.setMargins(0, dpToPx(8), 0, dpToPx(8));
            sep.setLayoutParams(lp);
            sep.setBackgroundColor(leagueId == ID_PREMIER ? 0x5537003C : 0xFF1F1F1F);
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            parent.addView(sep);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
<<<<<<< HEAD
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int colorLocal = 0xFFAAAAAA;
        int colorVisitante = 0xFFAAAAAA;

        try {
            float vL = Float.parseFloat(valLocal != null ? valLocal.replace("%", "") : "0");
            float vV = Float.parseFloat(valVis != null ? valVis.replace("%", "") : "0");

            if (mayorEsMejor ? vL > vV : vL < vV) {
                colorLocal = colorPrincipal;
            } else if (mayorEsMejor ? vV > vL : vV < vL) {
                colorVisitante = colorPrincipal;
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            }
        } catch (Exception ignored) {}

        TextView tvLocal = makeStatText(valLocal != null ? valLocal : "-", colorLocal, 60, Gravity.CENTER);
<<<<<<< HEAD
        TextView tvLabel = makeStatText(label, Color.WHITE, 0, Gravity.CENTER);
        TextView tvVis = makeStatText(valVis != null ? valVis : "-", colorVisitante, 60, Gravity.CENTER);

        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        tvLabel.setTextSize(13f);
=======
        TextView tvLabel = makeStatText(label, 0xFFAAAAAA, 0, Gravity.CENTER);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setTextSize(13f);
        TextView tvVis = makeStatText(valVis != null ? valVis : "-", colorVisitante, 60, Gravity.CENTER);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f

        fila.addView(tvLocal);
        fila.addView(tvLabel);
        fila.addView(tvVis);
<<<<<<< HEAD

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        parent.addView(fila);
    }

    private void actualizarBarra(String pLocal, String pEmpate, String pVis) {
        try {
<<<<<<< HEAD
            float l = pLocal != null ? Float.parseFloat(pLocal.replace("%", "")) : 33f;
            float e = pEmpate != null ? Float.parseFloat(pEmpate.replace("%", "")) : 33f;
            float v = pVis != null ? Float.parseFloat(pVis.replace("%", "")) : 34f;

            setBarraWeight(R.id.barraLocal, l);
            setBarraWeight(R.id.barraEmpate, e);
            setBarraWeight(R.id.barraVisitante, v);

            cambiarFondo(R.id.barraLocal, colorPrincipal);
            cambiarFondo(R.id.barraEmpate, colorSecundario);
            cambiarFondo(R.id.barraVisitante, colorTercero);

=======
            float l = pLocal  != null ? Float.parseFloat(pLocal.replace("%",""))  : 33f;
            float e = pEmpate != null ? Float.parseFloat(pEmpate.replace("%","")) : 33f;
            float v = pVis    != null ? Float.parseFloat(pVis.replace("%",""))    : 34f;

            setBarraWeight(R.id.barraLocal,     l);
            setBarraWeight(R.id.barraEmpate,    e);
            setBarraWeight(R.id.barraVisitante, v);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        } catch (Exception ex) {
            Log.e(TAG, "Error barra: " + ex.getMessage());
        }
    }

    private void setBarraWeight(int id, float weight) {
<<<<<<< HEAD
        View v = findViewById(id);
        if (v == null) return;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
        );

=======
        android.view.View v = findViewById(id);
        if (v == null) return;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, weight);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        v.setLayoutParams(lp);
    }

    private void pintarForma(int layoutId, String forma) {
        LinearLayout layout = findViewById(layoutId);
        if (layout == null) return;
<<<<<<< HEAD

        layout.removeAllViews();

        if (forma == null || forma.isEmpty()) return;

        String f = forma.length() > 5 ? forma.substring(forma.length() - 5) : forma;

=======
        layout.removeAllViews();
        if (forma == null || forma.isEmpty()) return;

        String f = forma.length() > 5 ? forma.substring(forma.length() - 5) : forma;
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        for (char c : f.toCharArray()) {
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(c));
            tv.setTextSize(12f);
<<<<<<< HEAD
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);

=======
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            int size = dpToPx(28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(dpToPx(2), 0, dpToPx(2), 0);
            tv.setLayoutParams(lp);
<<<<<<< HEAD

            if (c == 'W') {
                tv.setBackgroundColor(colorPrincipal);
                tv.setTextColor(leagueId == ID_PREMIER ? PREMIER_MORADO : Color.WHITE);
            } else if (c == 'D') {
                tv.setBackgroundColor(0xFF9E9E9E);
                tv.setTextColor(Color.WHITE);
            } else if (c == 'L') {
                tv.setBackgroundColor(colorSecundario);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundColor(0xFF333333);
                tv.setTextColor(Color.WHITE);
            }

=======
            if      (c == 'W') tv.setBackgroundColor(0xFF4CAF50);
            else if (c == 'D') tv.setBackgroundColor(0xFF9E9E9E);
            else if (c == 'L') tv.setBackgroundColor(0xFFF44336);
            else                tv.setBackgroundColor(0xFF333333);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            layout.addView(tv);
        }
    }

    private void pintarH2H(String[] resultados) {
        LinearLayout layout = findViewById(R.id.layoutH2H);
        if (layout == null) return;
<<<<<<< HEAD

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
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
<<<<<<< HEAD

=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        for (int i = 0; i < resultados.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(resultados[i]);
            tv.setTextColor(0xFFCCCCCC);
            tv.setTextSize(13f);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(8), 0, dpToPx(8));
<<<<<<< HEAD

            layout.addView(tv);

            if (i < resultados.length - 1) {
                View sep = new View(this);
                sep.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                ));
                sep.setBackgroundColor(leagueId == ID_PREMIER ? PREMIER_MORADO : 0xFF1F1F1F);
=======
            layout.addView(tv);
            if (i < resultados.length - 1) {
                View sep = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
                sep.setLayoutParams(lp);
                sep.setBackgroundColor(0xFF1F1F1F);
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                layout.addView(sep);
            }
        }
    }

    private void cargarOnce(ApiService api, int teamId, String nombre, boolean esLocal) {
<<<<<<< HEAD
        int fieldId = esLocal ? R.id.fieldViewLocal : R.id.fieldViewVisitante;
        int tituloId = esLocal ? R.id.txtTituloOnceLocal : R.id.txtTituloOnceVisitante;

        runOnUiThread(() -> {
            TextView titulo = findViewById(tituloId);
            if (titulo != null) {
                titulo.setText("ONCE PROBABLE — " + nombre.toUpperCase());
                titulo.setTextColor(colorPrincipal);
            }
        });

        final boolean esLocalFinal = esLocal;

=======
        int fieldId  = esLocal ? R.id.fieldViewLocal    : R.id.fieldViewVisitante;
        int tituloId = esLocal ? R.id.txtTituloOnceLocal : R.id.txtTituloOnceVisitante;

        // Actualizar título con nombre del equipo
        runOnUiThread(() -> {
            TextView titulo = findViewById(tituloId);
            if (titulo != null) titulo.setText("ONCE PROBABLE — " + nombre.toUpperCase());
        });

        final boolean esLocalFinal = esLocal;
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        api.getOnceProbable(leagueId, teamId, fixtureId).enqueue(new Callback<OnceProbable>() {
            @Override
            public void onResponse(Call<OnceProbable> call, Response<OnceProbable> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String escudo = esLocalFinal
                            ? getIntent().getStringExtra("LOCAL_ESCUDO")
                            : getIntent().getStringExtra("VISITANTE_ESCUDO");
<<<<<<< HEAD

                    mostrarOnceEnCampo(fieldId, response.body(), escudo != null ? escudo : "");
                }
            }

=======
                    mostrarOnceEnCampo(fieldId, response.body(), escudo != null ? escudo : "");
                }
            }
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
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

<<<<<<< HEAD
            List<FieldView.PlayerSlot> slots = new java.util.ArrayList<>();

=======
            // Convertir modelo OnceProbable.Jugador → FieldView.PlayerSlot
            List<FieldView.PlayerSlot> slots = new java.util.ArrayList<>();
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            if (once.jugadores != null) {
                for (OnceProbable.Jugador j : once.jugadores) {
                    slots.add(new FieldView.PlayerSlot(j.nombre, j.posicion, j.id));
                }
            }
<<<<<<< HEAD

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

=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
                startActivity(intent);
            });
        });
    }

<<<<<<< HEAD
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
=======

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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
    }

    private TextView makeStatText(String texto, int color, int widthDp, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(color);
        tv.setTextSize(15f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(gravity);
<<<<<<< HEAD

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
            tv.setTextColor(colorSecundario);
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

    private void cambiarFondo(int id, int color) {
        View v = findViewById(id);
        if (v != null) {
            v.setBackgroundColor(color);
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}