package com.example.tfg;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparadorActivity extends AppCompatActivity {

    private static final String TAG = "Comparador";

    // ── Vistas ──────────────────────────────────────────────────
    private EditText      etBuscar;
    private ListView      listResultados;
    private LinearLayout  contenedorComparativa;
    private LinearLayout  layoutPlaceholder;

    // ── Estado ──────────────────────────────────────────────────
    private EstadisticasJugador jugador1;
    private EstadisticasJugador jugador2;
    private int leagueId;

    // ── Búsqueda ─────────────────────────────────────────────────
    private List<JugadorBusqueda> resultadosBusqueda = new ArrayList<>();
    private ResultadoAdapter adapter;

    // ── Modelo ligero para resultados de búsqueda ────────────────
    public static class JugadorBusqueda {
        public int    id;
        public String nombre;
        public String equipo;
        public String foto;
        public String escudoEquipo;
        public JugadorBusqueda(int id, String nombre, String equipo, String foto, String escudoEquipo) {
            this.id = id; this.nombre = nombre; this.equipo = equipo;
            this.foto = foto; this.escudoEquipo = escudoEquipo;
        }
        @Override public String toString() { return nombre + " — " + equipo; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparador);

        // Datos del jugador 1 (venimos de EstadisticasJugadorActivity)
        leagueId = getIntent().getIntExtra("LEAGUE_ID", 140);
        jugador1 = new EstadisticasJugador();
        jugador1.nombre           = getIntent().getStringExtra("J1_NOMBRE");
        jugador1.foto             = getIntent().getStringExtra("J1_FOTO");
        jugador1.escudoEquipo     = getIntent().getStringExtra("J1_ESCUDO");
        jugador1.posicion         = getIntent().getStringExtra("J1_POSICION");
        jugador1.valoracion       = getIntent().getStringExtra("J1_VALORACION");
        jugador1.partidos         = getIntent().getIntExtra("J1_PARTIDOS", 0);
        jugador1.titularidades    = getIntent().getIntExtra("J1_TITULARIDADES", 0);
        jugador1.minutos          = getIntent().getIntExtra("J1_MINUTOS", 0);
        jugador1.goles            = getIntent().getIntExtra("J1_GOLES", 0);
        jugador1.asistencias      = getIntent().getIntExtra("J1_ASISTENCIAS", 0);
        jugador1.tirosTotales     = getIntent().getIntExtra("J1_TIROS", 0);
        jugador1.tirosPuerta      = getIntent().getIntExtra("J1_TIROS_PUERTA", 0);
        jugador1.pasesTotales     = getIntent().getIntExtra("J1_PASES", 0);
        jugador1.pasesClave       = getIntent().getIntExtra("J1_PASES_CLAVE", 0);
        jugador1.precisionPases   = getIntent().getIntExtra("J1_PRECISION_PASES", 0);
        jugador1.duelosTotales    = getIntent().getIntExtra("J1_DUELOS", 0);
        jugador1.duelosGanados    = getIntent().getIntExtra("J1_DUELOS_GANADOS", 0);
        jugador1.regatesIntentados= getIntent().getIntExtra("J1_REGATES", 0);
        jugador1.regatesExitosos  = getIntent().getIntExtra("J1_REGATES_OK", 0);
        jugador1.intercepciones   = getIntent().getIntExtra("J1_INTERCEPCIONES", 0);
        jugador1.faltasCometidas  = getIntent().getIntExtra("J1_FALTAS_COM", 0);
        jugador1.faltasRecibidas  = getIntent().getIntExtra("J1_FALTAS_REC", 0);
        jugador1.tarjetasAmarillas= getIntent().getIntExtra("J1_AMARILLAS", 0);
        jugador1.tarjetasRojas    = getIntent().getIntExtra("J1_ROJAS", 0);
        jugador1.penaltisConvertidos = getIntent().getIntExtra("J1_PENALTIS", 0);

        bindViews();
        mostrarJugador1();
        setupBusqueda();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        etBuscar              = findViewById(R.id.etBuscarJugador);
        listResultados        = findViewById(R.id.listResultados);
        contenedorComparativa = findViewById(R.id.contenedorComparativa);
        layoutPlaceholder     = findViewById(R.id.layoutPlaceholder);

        adapter = new ResultadoAdapter(this, resultadosBusqueda);
        listResultados.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────
    //  Jugador 1 (ya lo tenemos)
    // ─────────────────────────────────────────────

    private void mostrarJugador1() {
        cargarFoto(R.id.imgFoto1,   jugador1.foto);
        cargarFoto(R.id.imgEscudo1, jugador1.escudoEquipo);
        setText(R.id.txtNombre1,    jugador1.nombre);
        setText(R.id.txtPosicion1,  traducirPosicion(jugador1.posicion));
        setText(R.id.txtValoracion1, jugador1.valoracion != null ? jugador1.valoracion : "-");
        colorearValoracion(R.id.txtValoracion1, jugador1.valoracion);

        // Mostrar estadísticas iniciales solo del J1 (sin comparar)
        actualizarComparativa();
    }

    // ─────────────────────────────────────────────
    //  Búsqueda de jugador 2
    // ─────────────────────────────────────────────

    private void setupBusqueda() {
        Button btnBuscar = findViewById(R.id.btnBuscar);

        btnBuscar.setOnClickListener(v -> buscar());

        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                buscar();
                return true;
            }
            return false;
        });

        listResultados.setOnItemClickListener((parent, view, position, id) -> {
            JugadorBusqueda seleccionado = resultadosBusqueda.get(position);
            listResultados.setVisibility(View.GONE);
            cerrarTeclado();
            cargarJugador2(seleccionado);
        });
    }

    private void buscar() {
        String texto = etBuscar.getText().toString().trim();
        if (texto.length() < 3) {
            etBuscar.setError("Escribe al menos 3 letras");
            return;
        }
        cerrarTeclado();

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.buscarJugadores(texto, SEASON_ACTUAL()).enqueue(new Callback<BusquedaJugadores>() {
            @Override
            public void onResponse(Call<BusquedaJugadores> call,
                                   Response<BusquedaJugadores> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarResultadosBusqueda(response.body().jugadores);
                }
            }
            @Override
            public void onFailure(Call<BusquedaJugadores> call, Throwable t) {
                Log.e(TAG, "Fallo búsqueda: " + t.getMessage());
            }
        });
    }

    private void mostrarResultadosBusqueda(List<JugadorBusqueda> resultados) {
        runOnUiThread(() -> {
            resultadosBusqueda.clear();
            resultadosBusqueda.addAll(resultados);
            adapter.notifyDataSetChanged();

            if (resultados.isEmpty()) {
                listResultados.setVisibility(View.GONE);
                etBuscar.setError("Sin resultados");
            } else {
                listResultados.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cargarJugador2(JugadorBusqueda seleccionado) {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getEstadisticasJugador(seleccionado.id, leagueId).enqueue(new Callback<EstadisticasJugador>() {
            @Override
            public void onResponse(Call<EstadisticasJugador> call,
                                   Response<EstadisticasJugador> response) {
                if (response.isSuccessful() && response.body() != null) {
                    jugador2 = response.body();
                    // Completar con datos de búsqueda si la API no los devuelve
                    if (jugador2.foto == null || jugador2.foto.isEmpty())
                        jugador2.foto = seleccionado.foto;
                    if (jugador2.escudoEquipo == null || jugador2.escudoEquipo.isEmpty())
                        jugador2.escudoEquipo = seleccionado.escudoEquipo;
                    mostrarJugador2();
                    actualizarComparativa();
                }
            }
            @Override
            public void onFailure(Call<EstadisticasJugador> call, Throwable t) {
                Log.e(TAG, "Fallo jugador2: " + t.getMessage());
            }
        });
    }

    private void mostrarJugador2() {
        runOnUiThread(() -> {
            layoutPlaceholder.setVisibility(View.GONE);
            setVisible(R.id.imgFoto2);    setVisible(R.id.imgEscudo2);
            setVisible(R.id.txtNombre2);  setVisible(R.id.txtPosicion2);
            setVisible(R.id.txtValoracion2);

            cargarFoto(R.id.imgFoto2,   jugador2.foto);
            cargarFoto(R.id.imgEscudo2, jugador2.escudoEquipo);
            setText(R.id.txtNombre2,    jugador2.nombre);
            setText(R.id.txtPosicion2,  traducirPosicion(jugador2.posicion));
            setText(R.id.txtValoracion2, jugador2.valoracion != null ? jugador2.valoracion : "-");
            colorearValoracion(R.id.txtValoracion2, jugador2.valoracion);
        });
    }

    // ─────────────────────────────────────────────
    //  Comparativa de estadísticas
    // ─────────────────────────────────────────────

    private void actualizarComparativa() {
        runOnUiThread(() -> {
            contenedorComparativa.removeAllViews();

            // Si no hay jugador2, mostrar solo las stats del J1 sin comparar
            boolean comparando = jugador2 != null;

            addSeccion("⚽ ATAQUE");
            addFilaComp("Goles",          jugador1.goles,            comparando ? jugador2.goles            : -1, true);
            addFilaComp("Asistencias",    jugador1.asistencias,      comparando ? jugador2.asistencias      : -1, true);
            addFilaComp("Tiros totales",  jugador1.tirosTotales,     comparando ? jugador2.tirosTotales     : -1, true);
            addFilaComp("Tiros a puerta", jugador1.tirosPuerta,      comparando ? jugador2.tirosPuerta      : -1, true);
            addFilaComp("Penaltis",       jugador1.penaltisConvertidos, comparando ? jugador2.penaltisConvertidos : -1, true);

            addSeccion("↗️ PASES");
            addFilaComp("Pases totales",  jugador1.pasesTotales,     comparando ? jugador2.pasesTotales     : -1, true);
            addFilaComp("Pases clave",    jugador1.pasesClave,       comparando ? jugador2.pasesClave       : -1, true);
            addFilaCompPct("Precisión",   jugador1.precisionPases,   comparando ? jugador2.precisionPases   : -1, true);

            addSeccion("💪 DUELOS Y REGATES");
            addFilaComp("Duelos ganados", jugador1.duelosGanados,    comparando ? jugador2.duelosGanados    : -1, true);
            addFilaComp("Regates exitosos",jugador1.regatesExitosos, comparando ? jugador2.regatesExitosos  : -1, true);
            addFilaComp("Intercepciones", jugador1.intercepciones,   comparando ? jugador2.intercepciones   : -1, true);

            addSeccion("⏱️ PARTICIPACIÓN");
            addFilaComp("Partidos",       jugador1.partidos,         comparando ? jugador2.partidos         : -1, true);
            addFilaComp("Titularidades",  jugador1.titularidades,    comparando ? jugador2.titularidades    : -1, true);
            addFilaComp("Minutos",        jugador1.minutos,          comparando ? jugador2.minutos          : -1, true);

            addSeccion("🟨 DISCIPLINA");
            addFilaComp("Amarillas",      jugador1.tarjetasAmarillas,comparando ? jugador2.tarjetasAmarillas: -1, false);
            addFilaComp("Rojas",          jugador1.tarjetasRojas,    comparando ? jugador2.tarjetasRojas    : -1, false);
            addFilaComp("Faltas cometidas",jugador1.faltasCometidas, comparando ? jugador2.faltasCometidas  : -1, false);
            addFilaComp("Faltas recibidas",jugador1.faltasRecibidas, comparando ? jugador2.faltasRecibidas  : -1, true);
        });
    }

    private void addSeccion(String titulo) {
        // Espacio
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(8)));
        contenedorComparativa.addView(space);

        TextView tv = new TextView(this);
        tv.setText(titulo);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dpToPx(4), 0, dpToPx(8));
        tv.setLayoutParams(lp);
        contenedorComparativa.addView(tv);
    }

    /**
     * Fila de comparativa con barra proporcional.
     * val2 = -1 significa que no hay jugador 2 todavía.
     * mayorEsMejor: true = más es mejor (goles), false = menos es mejor (faltas)
     */
    private void addFilaComp(String label, int val1, int val2, boolean mayorEsMejor) {
        addFilaCompInternal(label, val1, val2, mayorEsMejor, false);
    }

    private void addFilaCompPct(String label, int val1, int val2, boolean mayorEsMejor) {
        addFilaCompInternal(label, val1, val2, mayorEsMejor, true);
    }

    private void addFilaCompInternal(String label, int val1, int val2,
                                     boolean mayorEsMejor, boolean esPorcentaje) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dpToPx(12));
        contenedor.setLayoutParams(lp);
        contenedor.setBackground(new android.graphics.drawable.ColorDrawable(0xFF111111));
        contenedor.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        boolean hayJ2 = val2 >= 0;

        // Determinar colores
        int color1 = 0xFF4FC3F7; // azul por defecto J1
        int color2 = 0xFF4CAF50; // verde por defecto J2

        if (hayJ2) {
            boolean j1Mejor = mayorEsMejor ? val1 > val2 : val1 < val2;
            boolean j2Mejor = mayorEsMejor ? val2 > val1 : val2 < val1;
            if (j1Mejor)       { color1 = 0xFF4FC3F7; color2 = 0xFF555555; }
            else if (j2Mejor)  { color1 = 0xFF555555; color2 = 0xFF4CAF50; }
            else                { color1 = 0xFFFFC107; color2 = 0xFFFFC107; } // empate amarillo
        }

        // Fila superior: label + valores
        LinearLayout filaTexto = new LinearLayout(this);
        filaTexto.setOrientation(LinearLayout.HORIZONTAL);
        filaTexto.setGravity(Gravity.CENTER_VERTICAL);
        filaTexto.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Valor J1
        TextView tvVal1 = new TextView(this);
        tvVal1.setText(esPorcentaje ? val1 + "%" : String.valueOf(val1));
        tvVal1.setTextColor(color1);
        tvVal1.setTextSize(15f);
        tvVal1.setTypeface(null, Typeface.BOLD);
        tvVal1.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(48),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvVal1.setGravity(Gravity.CENTER);
        filaTexto.addView(tvVal1);

        // Label central
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(0xFFAAAAAA);
        tvLabel.setTextSize(13f);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setGravity(Gravity.CENTER);
        filaTexto.addView(tvLabel);

        // Valor J2
        TextView tvVal2 = new TextView(this);
        tvVal2.setText(hayJ2 ? (esPorcentaje ? val2 + "%" : String.valueOf(val2)) : "-");
        tvVal2.setTextColor(color2);
        tvVal2.setTextSize(15f);
        tvVal2.setTypeface(null, Typeface.BOLD);
        tvVal2.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(48),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvVal2.setGravity(Gravity.CENTER);
        filaTexto.addView(tvVal2);

        contenedor.addView(filaTexto);

        // Barras proporcionales
        if (hayJ2 && (val1 > 0 || val2 > 0)) {
            int max = Math.max(val1, val2);
            float pct1 = max > 0 ? (float) val1 / max : 0f;
            float pct2 = max > 0 ? (float) val2 / max : 0f;

            LinearLayout filaBarra = new LinearLayout(this);
            filaBarra.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(4));
            lpB.setMargins(0, dpToPx(6), 0, 0);
            filaBarra.setLayoutParams(lpB);

            // Barra J1 (crece hacia la derecha desde el centro → de izquierda)
            View barra1 = new View(this);
            LinearLayout.LayoutParams lpBar1 = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, pct1);
            barra1.setLayoutParams(lpBar1);
            barra1.setBackgroundColor(color1 != 0xFF555555 ? color1 : 0xFF333333);

            // Espacio central
            View espacioCentro = new View(this);
            espacioCentro.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(4), LinearLayout.LayoutParams.MATCH_PARENT));

            // Barra J2
            View barra2 = new View(this);
            LinearLayout.LayoutParams lpBar2 = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, pct2);
            barra2.setLayoutParams(lpBar2);
            barra2.setBackgroundColor(color2 != 0xFF555555 ? color2 : 0xFF333333);

            filaBarra.addView(barra1);
            filaBarra.addView(espacioCentro);
            filaBarra.addView(barra2);
            contenedor.addView(filaBarra);
        }

        contenedorComparativa.addView(contenedor);
    }

    // ─────────────────────────────────────────────
    //  Adapter para resultados de búsqueda
    // ─────────────────────────────────────────────

    private class ResultadoAdapter extends ArrayAdapter<JugadorBusqueda> {
        ResultadoAdapter(Context ctx, List<JugadorBusqueda> items) {
            super(ctx, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JugadorBusqueda item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView t1 = convertView.findViewById(android.R.id.text1);
            TextView t2 = convertView.findViewById(android.R.id.text2);
            t1.setTextColor(0xFFFFFFFF);
            t2.setTextColor(0xFFAAAAAA);
            t1.setText(item != null ? item.nombre : "");
            t2.setText(item != null ? item.equipo : "");
            convertView.setBackgroundColor(0xFF1A1A1A);
            return convertView;
        }
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private int SEASON_ACTUAL() { return 2025; }

    private void cargarFoto(int id, String url) {
        ImageView iv = findViewById(id);
        if (iv != null && url != null && !url.isEmpty()) {
            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
        }
    }

    private void setText(int id, String val) {
        TextView tv = findViewById(id);
        if (tv != null && val != null) tv.setText(val);
    }

    private void setVisible(int id) {
        View v = findViewById(id);
        if (v != null) v.setVisibility(View.VISIBLE);
    }

    private void colorearValoracion(int id, String val) {
        TextView tv = findViewById(id);
        if (tv == null || val == null) return;
        try {
            float nota = Float.parseFloat(val);
            if      (nota >= 7.5f) tv.setBackgroundColor(0xFF4CAF50);
            else if (nota >= 6.5f) tv.setBackgroundColor(0xFFFFC107);
            else                   tv.setBackgroundColor(0xFFF44336);
        } catch (Exception ignored) {
            tv.setBackgroundColor(0xFF555555);
        }
    }

    private String traducirPosicion(String pos) {
        if (pos == null) return "";
        switch (pos) {
            case "G": case "Goalkeeper": return "Portero";
            case "D": case "Defender":   return "Defensa";
            case "M": case "Midfielder": return "Centrocampista";
            case "F": case "Attacker":   return "Delantero";
            default: return pos;
        }
    }

    private void cerrarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && etBuscar != null)
            imm.hideSoftInputFromWindow(etBuscar.getWindowToken(), 0);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}