package com.example.tfg;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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

    private static final int ID_LALIGA = 140;
    private static final int ID_PREMIER = 39;

    private Spinner spinnerLiga1;
    private Spinner spinnerLiga2;

    private EditText etJugador1;
    private EditText etJugador2;

    private Button btnBuscar1;
    private Button btnBuscar2;

    private ListView listJugador1;
    private ListView listJugador2;

    private LinearLayout cardJugador1;
    private LinearLayout cardJugador2;

    private final List<JugadorBusqueda> resultados1 = new ArrayList<>();
    private final List<JugadorBusqueda> resultados2 = new ArrayList<>();

    private ArrayAdapter<JugadorBusqueda> adapter1;
    private ArrayAdapter<JugadorBusqueda> adapter2;

    private EstadisticasJugador jugador1;
    private EstadisticasJugador jugador2;

    private int ligaJugador1 = ID_LALIGA;
    private int ligaJugador2 = ID_PREMIER;

    public static class JugadorBusqueda {
        public int id;
        public String nombre;
        public String equipo;
        public String foto;
        public String escudoEquipo;

        public JugadorBusqueda(int id, String nombre, String equipo, String foto, String escudoEquipo) {
            this.id = id;
            this.nombre = nombre;
            this.equipo = equipo;
            this.foto = foto;
            this.escudoEquipo = escudoEquipo;
        }

        @Override
        public String toString() {
            return nombre + " — " + equipo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparador);

        bindViews();
        configurarSpinners();
        configurarListas();
        configurarBotones();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        mostrarPlaceholder(cardJugador1, "Jugador 1");
        mostrarPlaceholder(cardJugador2, "Jugador 2");
    }

    private void bindViews() {
        spinnerLiga1 = findViewById(R.id.spinnerLiga1);
        spinnerLiga2 = findViewById(R.id.spinnerLiga2);

        etJugador1 = findViewById(R.id.etJugador1);
        etJugador2 = findViewById(R.id.etJugador2);

        btnBuscar1 = findViewById(R.id.btnBuscar1);
        btnBuscar2 = findViewById(R.id.btnBuscar2);

        listJugador1 = findViewById(R.id.listJugador1);
        listJugador2 = findViewById(R.id.listJugador2);

        cardJugador1 = findViewById(R.id.cardJugador1);
        cardJugador2 = findViewById(R.id.cardJugador2);
    }

    private void configurarSpinners() {
        String[] ligas = {"LaLiga", "Premier"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                ligas
        );

        spinnerLiga1.setAdapter(adapter);
        spinnerLiga2.setAdapter(adapter);

        spinnerLiga1.setSelection(0);
        spinnerLiga2.setSelection(1);
    }

    private void configurarListas() {
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultados1);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultados2);

        listJugador1.setAdapter(adapter1);
        listJugador2.setAdapter(adapter2);

        listJugador1.setOnItemClickListener((parent, view, position, id) -> {
            JugadorBusqueda jugador = resultados1.get(position);
            ligaJugador1 = obtenerLigaSeleccionada(spinnerLiga1);
            listJugador1.setVisibility(View.GONE);
            cerrarTeclado();
            cargarEstadisticasJugador(jugador, ligaJugador1, true);
        });

        listJugador2.setOnItemClickListener((parent, view, position, id) -> {
            JugadorBusqueda jugador = resultados2.get(position);
            ligaJugador2 = obtenerLigaSeleccionada(spinnerLiga2);
            listJugador2.setVisibility(View.GONE);
            cerrarTeclado();
            cargarEstadisticasJugador(jugador, ligaJugador2, false);
        });
    }

    private void configurarBotones() {
        btnBuscar1.setOnClickListener(v ->
                buscarJugador(etJugador1, resultados1, adapter1, listJugador1)
        );

        btnBuscar2.setOnClickListener(v ->
                buscarJugador(etJugador2, resultados2, adapter2, listJugador2)
        );
    }

    private int obtenerLigaSeleccionada(Spinner spinner) {
        String liga = spinner.getSelectedItem().toString();

        if (liga.equalsIgnoreCase("Premier")) {
            return ID_PREMIER;
        }

        return ID_LALIGA;
    }

    private void buscarJugador(EditText editText,
                               List<JugadorBusqueda> resultados,
                               ArrayAdapter<JugadorBusqueda> adapter,
                               ListView listView) {

        String texto = editText.getText().toString().trim();

        if (texto.length() < 3) {
            editText.setError("Escribe mínimo 3 letras");
            return;
        }

        cerrarTeclado();

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.buscarJugadores(texto, 2025).enqueue(new Callback<BusquedaJugadores>() {
            @Override
            public void onResponse(Call<BusquedaJugadores> call,
                                   Response<BusquedaJugadores> response) {

                resultados.clear();

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().jugadores != null) {

                    resultados.addAll(response.body().jugadores);
                }

                adapter.notifyDataSetChanged();

                if (resultados.isEmpty()) {
                    editText.setError("Sin resultados");
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<BusquedaJugadores> call, Throwable t) {
                editText.setError("Error de conexión");
                listView.setVisibility(View.GONE);
            }
        });
    }

    private void cargarEstadisticasJugador(JugadorBusqueda jugadorBusqueda,
                                           int leagueId,
                                           boolean esJugador1) {

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        api.getEstadisticasJugador(jugadorBusqueda.id, leagueId).enqueue(new Callback<EstadisticasJugador>() {
            @Override
            public void onResponse(Call<EstadisticasJugador> call,
                                   Response<EstadisticasJugador> response) {

                if (response.isSuccessful() && response.body() != null) {
                    EstadisticasJugador stats = response.body();

                    if (stats.nombre == null || stats.nombre.isEmpty()) {
                        stats.nombre = jugadorBusqueda.nombre;
                    }

                    if (stats.foto == null || stats.foto.isEmpty()) {
                        stats.foto = jugadorBusqueda.foto;
                    }

                    if (stats.escudoEquipo == null || stats.escudoEquipo.isEmpty()) {
                        stats.escudoEquipo = jugadorBusqueda.escudoEquipo;
                    }

                    if (esJugador1) {
                        jugador1 = stats;
                    } else {
                        jugador2 = stats;
                    }

                    actualizarTarjetas();
                }
            }

            @Override
            public void onFailure(Call<EstadisticasJugador> call, Throwable t) {
                if (esJugador1) {
                    mostrarPlaceholder(cardJugador1, "Error jugador 1");
                } else {
                    mostrarPlaceholder(cardJugador2, "Error jugador 2");
                }
            }
        });
    }

    private void actualizarTarjetas() {
        if (jugador1 != null) {
            pintarTarjeta(cardJugador1, jugador1, ligaJugador1, jugador2);
        }

        if (jugador2 != null) {
            pintarTarjeta(cardJugador2, jugador2, ligaJugador2, jugador1);
        }
    }

    private void pintarTarjeta(LinearLayout card,
                               EstadisticasJugador jugador,
                               int leagueId,
                               EstadisticasJugador rival) {

        card.removeAllViews();
        card.setGravity(Gravity.NO_GRAVITY);
        card.setBackgroundColor(0xFF17172A);

        int colorLiga = leagueId == ID_PREMIER ? 0xFF00FF85 : 0xFFFF4B4B;
        int colorSecundario = leagueId == ID_PREMIER ? 0xFFFF2882 : 0xFF9C27FF;

        LinearLayout filaSuperior = new LinearLayout(this);
        filaSuperior.setOrientation(LinearLayout.HORIZONTAL);
        filaSuperior.setGravity(Gravity.CENTER_VERTICAL);

        ImageView escudo = new ImageView(this);
        escudo.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30)));

        if (jugador.escudoEquipo != null && !jugador.escudoEquipo.isEmpty()) {
            Glide.with(this)
                    .load(jugador.escudoEquipo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(escudo);
        }

        TextView dorsal = new TextView(this);
        dorsal.setText(jugador.numero > 0 ? String.valueOf(jugador.numero) : "-");
        dorsal.setTextColor(Color.WHITE);
        dorsal.setTextSize(18f);
        dorsal.setGravity(Gravity.END);
        dorsal.setTypeface(null, Typeface.BOLD);
        dorsal.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        filaSuperior.addView(escudo);
        filaSuperior.addView(dorsal);
        card.addView(filaSuperior);

        ImageView foto = new ImageView(this);
        foto.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(120)
        ));
        foto.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (jugador.foto != null && !jugador.foto.isEmpty()) {
            Glide.with(this)
                    .load(jugador.foto)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(foto);
        }

        card.addView(foto);

        TextView nombre = new TextView(this);
        nombre.setText(jugador.nombre != null ? jugador.nombre : "Jugador");
        nombre.setTextColor(Color.WHITE);
        nombre.setTextSize(15f);
        nombre.setTypeface(null, Typeface.BOLD);
        nombre.setGravity(Gravity.CENTER);
        nombre.setMaxLines(1);
        card.addView(nombre);

        TextView posicion = new TextView(this);
        posicion.setText(traducirPosicion(jugador.posicion));
        posicion.setTextColor(Color.WHITE);
        posicion.setTextSize(15f);
        posicion.setGravity(Gravity.CENTER);
        posicion.setPadding(0, dpToPx(4), 0, dpToPx(12));
        card.addView(posicion);

        addStat(card, "Partidos", jugador.partidos, rival != null ? rival.partidos : -1, colorLiga, true);
        addStat(card, "Minutos", jugador.minutos, rival != null ? rival.minutos : -1, 0xFF00BCD4, true);
        addStat(card, "Amarillas", jugador.tarjetasAmarillas, rival != null ? rival.tarjetasAmarillas : -1, 0xFFFFC107, false);
        addStat(card, "Faltas cometidas", jugador.faltasCometidas, rival != null ? rival.faltasCometidas : -1, 0xFFFFD600, false);
        addStat(card, "Faltas recibidas", jugador.faltasRecibidas, rival != null ? rival.faltasRecibidas : -1, 0xFFFFD600, true);
        addStat(card, "Goles", jugador.goles, rival != null ? rival.goles : -1, colorLiga, true);
        addStat(card, "Asistencias", jugador.asistencias, rival != null ? rival.asistencias : -1, colorSecundario, true);
        addStat(card, "Pases", jugador.pasesTotales, rival != null ? rival.pasesTotales : -1, 0xFF00BCD4, true);
    }

    private void addStat(LinearLayout parent,
                         String label,
                         int valor,
                         int valorRival,
                         int colorBarra,
                         boolean mayorEsMejor) {

        TextView titulo = new TextView(this);
        titulo.setText(label);
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(11f);
        titulo.setPadding(0, dpToPx(5), 0, 0);
        parent.addView(titulo);

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        int ancho = 45;

        if (valorRival >= 0) {
            int max = Math.max(valor, valorRival);
            if (max > 0) {
                ancho = Math.max(12, Math.round((valor * 100f) / max));
            }
        }

        View barra = new View(this);
        barra.setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(4), ancho));
        barra.setBackgroundColor(colorBarra);

        TextView numero = new TextView(this);
        numero.setText(String.valueOf(valor));
        numero.setTextColor(Color.WHITE);
        numero.setTextSize(11f);
        numero.setGravity(Gravity.END);
        numero.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                100 - ancho
        ));

        if (valorRival >= 0) {
            boolean mejor = mayorEsMejor ? valor > valorRival : valor < valorRival;
            boolean empate = valor == valorRival;

            if (mejor) {
                numero.setTextColor(0xFF00FF85);
            } else if (empate) {
                numero.setTextColor(0xFFFFC107);
            } else {
                numero.setTextColor(0xFFFF2882);
            }
        }

        fila.addView(barra);
        fila.addView(numero);
        parent.addView(fila);
    }

    private void mostrarPlaceholder(LinearLayout card, String texto) {
        card.removeAllViews();
        card.setGravity(Gravity.CENTER);
        card.setMinimumHeight(dpToPx(330));
        card.setBackgroundColor(0xFF17172A);

        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15f);
        tv.setGravity(Gravity.CENTER);

        card.addView(tv);
    }

    private String traducirPosicion(String pos) {
        if (pos == null) return "Desconocido";

        switch (pos) {
            case "G":
            case "Goalkeeper":
                return "Portero";
            case "D":
            case "Defender":
                return "Defensa";
            case "M":
            case "Midfielder":
                return "Centrocampista";
            case "F":
            case "Attacker":
                return "Delantero";
            default:
                return pos;
        }
    }
    private void cerrarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}