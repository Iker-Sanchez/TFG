package com.example.tfg;

import android.content.Intent;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasJugadorActivity extends AppCompatActivity {

    private static final String TAG = "EstJugador";

    private static final int ID_LALIGA = 140;
    private static final int ID_PREMIER = 39;

    private static final int LALIGA_ROJO = 0xFFE30613;
    private static final int LALIGA_CARD = 0xFF111111;

    private static final int PREMIER_MORADO = 0xFF37003C;
    private static final int PREMIER_VERDE = 0xFF00FF85;
    private static final int PREMIER_ROSA = 0xFFFF2882;
    private static final int PREMIER_FONDO = 0xFF050006;
    private static final int PREMIER_CARD = 0xFF120016;

    private int playerId;
    private int leagueId;
    private EstadisticasJugador statsActuales;

    private int colorPrincipal;
    private int colorSecundario;
    private int colorHeader;
    private int colorFondo;
    private int colorCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_jugador);

        playerId = getIntent().getIntExtra("PLAYER_ID", 0);
        leagueId = getIntent().getIntExtra("LEAGUE_ID", ID_LALIGA);

        String nombre = getIntent().getStringExtra("PLAYER_NOMBRE");
        String foto = getIntent().getStringExtra("PLAYER_FOTO");
        String escudo = getIntent().getStringExtra("EQUIPO_ESCUDO");

        configurarTemaLiga();
        aplicarTemaLiga();

        setText(R.id.txtNombreJugador, nombre != null ? nombre : "Jugador");
        setText(R.id.txtValoracion, "-");

        if (foto != null && !foto.isEmpty()) {
            Glide.with(this)
                    .load(foto)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgFotoJugador));
        }

        if (escudo != null && !escudo.isEmpty()) {
            Glide.with(this)
                    .load(escudo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgEscudoEquipo));
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageView btnComparar = findViewById(R.id.btnComparar);
        if (btnComparar != null) {
            btnComparar.setAlpha(0.5f);
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

                    runOnUiThread(() -> {
                        ImageView btn = findViewById(R.id.btnComparar);
                        if (btn != null) {
                            btn.setAlpha(1.0f);
                        }
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

    private void configurarTemaLiga() {
        if (leagueId == ID_PREMIER) {
            colorPrincipal = PREMIER_VERDE;
            colorSecundario = PREMIER_ROSA;
            colorHeader = PREMIER_MORADO;
            colorFondo = PREMIER_FONDO;
            colorCard = PREMIER_CARD;
        } else {
            colorPrincipal = Color.WHITE;
            colorSecundario = LALIGA_ROJO;
            colorHeader = LALIGA_ROJO;
            colorFondo = Color.BLACK;
            colorCard = LALIGA_CARD;
        }
    }

    private void aplicarTemaLiga() {
        cambiarFondo(R.id.rootEstadisticasJugador, colorFondo);
        cambiarFondo(R.id.scrollEstadisticasJugador, colorFondo);
        cambiarFondo(R.id.contenidoEstadisticasJugador, colorFondo);

        cambiarFondo(R.id.headerEstadisticasJugador, colorHeader);

        if (leagueId == ID_PREMIER) {
            cambiarColorTexto(R.id.txtTituloJugadorHeader, PREMIER_VERDE);

            setText(R.id.txtLogoLigaJugador, "PREMIER");
            cambiarFondo(R.id.txtLogoLigaJugador, PREMIER_VERDE);
            cambiarColorTexto(R.id.txtLogoLigaJugador, PREMIER_MORADO);

            cambiarColorTexto(R.id.txtNombreJugador, PREMIER_VERDE);
        } else {
            cambiarColorTexto(R.id.txtTituloJugadorHeader, Color.WHITE);

            setText(R.id.txtLogoLigaJugador, "LALIGA");
            cambiarFondo(R.id.txtLogoLigaJugador, 0xFF111111);
            cambiarColorTexto(R.id.txtLogoLigaJugador, Color.WHITE);

            cambiarColorTexto(R.id.txtNombreJugador, Color.WHITE);
        }

        cambiarFondo(R.id.cardPerfilJugador, colorCard);
        cambiarFondo(R.id.cardParticipacion, colorCard);
        cambiarFondo(R.id.contenedorAtaque, colorCard);
        cambiarFondo(R.id.contenedorPases, colorCard);
        cambiarFondo(R.id.contenedorDuelos, colorCard);
        cambiarFondo(R.id.contenedorDisciplina, colorCard);

        cambiarColorTexto(R.id.txtPartidos, leagueId == ID_PREMIER ? PREMIER_VERDE : Color.WHITE);
        cambiarColorTexto(R.id.txtTitularidades, leagueId == ID_PREMIER ? PREMIER_VERDE : Color.WHITE);
        cambiarColorTexto(R.id.txtMinutos, leagueId == ID_PREMIER ? PREMIER_VERDE : Color.WHITE);
    }

    private void mostrarEstadisticas(EstadisticasJugador e) {
        setText(R.id.txtNombreJugador, e.nombre);
        setText(R.id.txtPosicion, "Pos: " + traducirPosicion(e.posicion));
        setText(R.id.txtEdad, "Edad: " + e.edad + " años");
        setText(R.id.txtNacionalidad, "Nac: " + e.nacionalidad);
        setText(R.id.txtValoracion, e.valoracion != null ? e.valoracion : "-");

        colorearValoracion(e.valoracion);

        if (e.foto != null && !e.foto.isEmpty()) {
            Glide.with(this)
                    .load(e.foto)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgFotoJugador));
        }

        if (e.escudoEquipo != null && !e.escudoEquipo.isEmpty()) {
            Glide.with(this)
                    .load(e.escudoEquipo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.imgEscudoEquipo));
        }

        setText(R.id.txtPartidos, String.valueOf(e.partidos));
        setText(R.id.txtTitularidades, String.valueOf(e.titularidades));
        setText(R.id.txtMinutos, String.valueOf(e.minutos));

        LinearLayout cAtaque = findViewById(R.id.contenedorAtaque);
        LinearLayout cPases = findViewById(R.id.contenedorPases);
        LinearLayout cDuelos = findViewById(R.id.contenedorDuelos);
        LinearLayout cDisc = findViewById(R.id.contenedorDisciplina);

        cAtaque.removeAllViews();
        cPases.removeAllViews();
        cDuelos.removeAllViews();
        cDisc.removeAllViews();

        int colorDato = leagueId == ID_PREMIER ? PREMIER_VERDE : Color.WHITE;
        int colorEspecial = leagueId == ID_PREMIER ? PREMIER_ROSA : LALIGA_ROJO;

        addFila(cAtaque, "⚽  Goles", String.valueOf(e.goles), colorEspecial);
        addFila(cAtaque, "🎯  Asistencias", String.valueOf(e.asistencias), colorDato);
        addFila(cAtaque, "🔫  Tiros totales", String.valueOf(e.tirosTotales), Color.WHITE);
        addFila(cAtaque, "🎯  Tiros a puerta", String.valueOf(e.tirosPuerta), Color.WHITE);

        if (e.penaltisConvertidos > 0 || e.penaltisFallados > 0) {
            addFila(
                    cAtaque,
                    "⚡  Penaltis",
                    e.penaltisConvertidos + " gol / " + e.penaltisFallados + " fallo",
                    colorDato
            );
        }

        addFila(cPases, "↗️  Pases totales", String.valueOf(e.pasesTotales), Color.WHITE);
        addFila(cPases, "🔑  Pases clave", String.valueOf(e.pasesClave), colorDato);
        addFila(cPases, "✅  Precisión pases", e.precisionPases + "%", Color.WHITE);

        int pctDuelos = e.duelosTotales > 0
                ? Math.round((e.duelosGanados * 100f) / e.duelosTotales)
                : 0;

        addFila(
                cDuelos,
                "💪  Duelos ganados",
                e.duelosGanados + " / " + e.duelosTotales + " (" + pctDuelos + "%)",
                Color.WHITE
        );

        int pctRegates = e.regatesIntentados > 0
                ? Math.round((e.regatesExitosos * 100f) / e.regatesIntentados)
                : 0;

        addFila(
                cDuelos,
                "⚡  Regates exitosos",
                e.regatesExitosos + " / " + e.regatesIntentados + " (" + pctRegates + "%)",
                colorDato
        );

        addFila(cDuelos, "🛡️  Intercepciones", String.valueOf(e.intercepciones), Color.WHITE);
        addFila(cDuelos, "🦶  Faltas recibidas", String.valueOf(e.faltasRecibidas), Color.WHITE);

        addFila(cDisc, "🟨  Amarillas", String.valueOf(e.tarjetasAmarillas), 0xFFFFC107);
        addFila(cDisc, "🟥  Rojas", String.valueOf(e.tarjetasRojas), 0xFFF44336);
        addFila(cDisc, "🦶  Faltas cometidas", String.valueOf(e.faltasCometidas), Color.WHITE);

        aplicarTemaLiga();
    }

    private void colorearValoracion(String val) {
        TextView tv = findViewById(R.id.txtValoracion);
        if (tv == null) return;

        try {
            float nota = Float.parseFloat(val);
            int color;

            if (leagueId == ID_PREMIER) {
                if (nota >= 7.5f) {
                    color = PREMIER_VERDE;
                    tv.setTextColor(PREMIER_MORADO);
                } else if (nota >= 6.5f) {
                    color = PREMIER_ROSA;
                    tv.setTextColor(Color.WHITE);
                } else {
                    color = 0xFFE53935;
                    tv.setTextColor(Color.WHITE);
                }
            } else {
                if (nota >= 7.5f) color = 0xFF4CAF50;
                else if (nota >= 6.5f) color = 0xFFFFC107;
                else color = 0xFFF44336;

                tv.setTextColor(Color.WHITE);
            }

            tv.setBackgroundColor(color);

        } catch (Exception ignored) {
            tv.setBackgroundColor(0xFF555555);
            tv.setTextColor(Color.WHITE);
        }
    }

    private void abrirComparador() {
        Intent intent = new Intent(this, ComparadorActivity.class);
        startActivity(intent);
    }

    private void addFila(LinearLayout parent, String label, String valor, int colorValor) {
        if (parent.getChildCount() > 0) {
            View sep = new View(this);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
            );

            lp.setMargins(0, dpToPx(10), 0, dpToPx(10));
            sep.setLayoutParams(lp);

            sep.setBackgroundColor(leagueId == ID_PREMIER ? PREMIER_MORADO : 0xFF1F1F1F);

            parent.addView(sep);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(0xFFCCCCCC);
        tvLabel.setTextSize(14f);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

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
        }
    }

    private void mostrarError(String msg) {
        runOnUiThread(() -> {
            LinearLayout c = findViewById(R.id.contenedorAtaque);
            if (c == null) return;

            c.removeAllViews();

            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextColor(leagueId == ID_PREMIER ? PREMIER_ROSA : 0xFFFF6B6B);
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