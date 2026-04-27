package com.example.tfg;

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

public class EstadisticasPartidoActivity extends AppCompatActivity {

    private static final String TAG = "EstPartido";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_partido);

        int    fixtureId       = getIntent().getIntExtra("FIXTURE_ID", 0);
        String localNombre     = getIntent().getStringExtra("LOCAL_NOMBRE");
        String localEscudo     = getIntent().getStringExtra("LOCAL_ESCUDO");
        String visitanteNombre = getIntent().getStringExtra("VISITANTE_NOMBRE");
        String visitanteEscudo = getIntent().getStringExtra("VISITANTE_ESCUDO");
        int    golesLocal      = getIntent().getIntExtra("GOLES_LOCAL", 0);
        int    golesVisitante  = getIntent().getIntExtra("GOLES_VISITANTE", 0);

        cargarEscudo(findViewById(R.id.imgEscudoLocal),     localEscudo);
        cargarEscudo(findViewById(R.id.imgEscudoVisitante), visitanteEscudo);

        TextView tvLocal = findViewById(R.id.txtNombreLocal);
        TextView tvVis   = findViewById(R.id.txtNombreVisitante);
        tvLocal.setText(localNombre);
        tvVis.setText(visitanteNombre);

        ((TextView) findViewById(R.id.txtResultado))
                .setText(golesLocal + " - " + golesVisitante);

        // Colorear ganador
        if (golesLocal > golesVisitante)      tvLocal.setTextColor(0xFF4CAF50);
        else if (golesVisitante > golesLocal) tvVis.setTextColor(0xFF4CAF50);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Log.d(TAG, "Cargando stats partido fixtureId=" + fixtureId);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getEstadisticasPartido(fixtureId).enqueue(new Callback<EstadisticasPartido>() {
            @Override
            public void onResponse(Call<EstadisticasPartido> call,
                                   Response<EstadisticasPartido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarEstadisticas(response.body());
                } else {
                    Log.e(TAG, "Error HTTP: " + response.code());
                    mostrarError("No hay estadísticas disponibles");
                }
            }

            @Override
            public void onFailure(Call<EstadisticasPartido> call, Throwable t) {
                Log.e(TAG, "Fallo: " + t.getMessage());
                mostrarError("Error de conexión");
            }
        });
    }

    private void mostrarEstadisticas(EstadisticasPartido e) {
        LinearLayout contenedor = findViewById(R.id.contenedorStats);
        contenedor.removeAllViews();

        // Posesión — mayor NO siempre es mejor, pero es neutro; lo mostramos sin colorear como mejor
        addFila(contenedor, e.posesionLocal + "%",          "Posesión",          e.posesionVisitante + "%",          true);
        addFila(contenedor, String.valueOf(e.tirosLocal),   "Tiros totales",     String.valueOf(e.tirosVisitante),   true);
        addFila(contenedor, String.valueOf(e.tirosPuertaLocal), "Tiros a puerta",String.valueOf(e.tirosPuertaVisitante), true);
        addFila(contenedor, String.valueOf(e.pasesLocal),   "Pases",             String.valueOf(e.pasesVisitante),   true);
        addFila(contenedor, e.precisionPasesLocal + "%",    "Precisión pases",   e.precisionPasesVisitante + "%",    true);
        addFila(contenedor, String.valueOf(e.cornersLocal), "Córners",           String.valueOf(e.cornersVisitante), true);
        // Faltas y tarjetas: menos es mejor
        addFila(contenedor, String.valueOf(e.faltasLocal),    "Faltas",          String.valueOf(e.faltasVisitante),    false);
        addFila(contenedor, String.valueOf(e.amarillasLocal), "🟨 Amarillas",    String.valueOf(e.amarillasVisitante), false);
        addFila(contenedor, String.valueOf(e.rojasLocal),     "🟥 Rojas",        String.valueOf(e.rojasVisitante),     false);
        addFila(contenedor, String.valueOf(e.offsidesLocal),  "Fueras de juego", String.valueOf(e.offsidesVisitante),  false);
    }

    /** Crea y añade una fila de estadística al contenedor */
    private void addFila(LinearLayout parent,
                         String valLocal, String label, String valVis,
                         boolean mayorEsMejor) {
        // Separador
        if (parent.getChildCount() > 0) {
            View sep = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            lp.setMargins(0, dpToPx(10), 0, dpToPx(10));
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

        // Determinar colores
        int colorLocal = 0xFFCCCCCC, colorVis = 0xFFCCCCCC;
        try {
            float vL = Float.parseFloat(valLocal.replace("%","").trim());
            float vV = Float.parseFloat(valVis.replace("%","").trim());
            boolean localMejor = mayorEsMejor ? vL > vV : vL < vV;
            boolean visMejor   = mayorEsMejor ? vV > vL : vV < vL;
            if (localMejor)  colorLocal = 0xFF4FC3F7;
            if (visMejor)    colorVis   = 0xFF4CAF50;
        } catch (Exception ignored) {}

        TextView tvLocal = crearTexto(valLocal, colorLocal, dpToPx(60), Gravity.CENTER);
        TextView tvLabel = crearTexto(label,    0xFFAAAAAA, 0,          Gravity.CENTER);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setTextSize(13f);
        tvLabel.setTypeface(null, Typeface.NORMAL);
        TextView tvVis = crearTexto(valVis, colorVis, dpToPx(60), Gravity.CENTER);

        fila.addView(tvLocal);
        fila.addView(tvLabel);
        fila.addView(tvVis);
        parent.addView(fila);
    }

    private TextView crearTexto(String texto, int color, int widthPx, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(color);
        tv.setTextSize(15f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(gravity);
        if (widthPx > 0) {
            tv.setLayoutParams(new LinearLayout.LayoutParams(widthPx,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        return tv;
    }

    private void mostrarError(String msg) {
        runOnUiThread(() -> {
            LinearLayout c = findViewById(R.id.contenedorStats);
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

    private void cargarEscudo(ImageView iv, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}