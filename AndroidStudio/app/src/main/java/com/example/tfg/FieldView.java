package com.example.tfg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista personalizada que dibuja un campo de fútbol con los jugadores
 * posicionados según su línea táctica (portero, defensas, medios, delanteros).
 *
 * Uso en XML:
 *   <com.example.tfg.FieldView
 *       android:id="@+id/fieldView"
 *       android:layout_width="match_parent"
 *       android:layout_height="320dp" />
 *
 * Uso en Java:
 *   fieldView.setLineup(formacion, listaJugadores);
 */
public class FieldView extends View {

    // ── Modelo interno ──────────────────────────────────────────
    public static class PlayerSlot {
        public String nombre;
        public String posicion; // "G", "D", "M", "F"
        public int    playerId;
        public PlayerSlot(String nombre, String posicion, int playerId) {
            this.nombre   = nombre;
            this.posicion = posicion;
            this.playerId = playerId;
        }
    }

    public interface OnPlayerClickListener {
        void onPlayerClick(PlayerSlot player);
    }

    private OnPlayerClickListener clickListener;

    public void setOnPlayerClickListener(OnPlayerClickListener listener) {
        this.clickListener = listener;
    }

    // ── Estado ──────────────────────────────────────────────────
    private String            formacion  = "4-3-3";
    private List<PlayerSlot>  jugadores  = new ArrayList<>();

    // ── Pinturas ────────────────────────────────────────────────
    private final Paint paintCesped    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLineas    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCirculo   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNumero    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNombre    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSombra    = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Colores por posición ────────────────────────────────────
    private static final int COLOR_PORTERO   = 0xFFFFEB3B;
    private static final int COLOR_DEFENSA   = 0xFF4FC3F7;
    private static final int COLOR_MEDIO     = 0xFF81C784;
    private static final int COLOR_DELANTERO = 0xFFEF9A9A;
    private static final int COLOR_CAMPO     = 0xFF2E7D32;
    private static final int COLOR_CAMPO2    = 0xFF1B5E20;

    public FieldView(Context context) {
        super(context);
        init();
    }

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintLineas.setColor(Color.WHITE);
        paintLineas.setStrokeWidth(3f);
        paintLineas.setStyle(Paint.Style.STROKE);

        paintCesped.setStyle(Paint.Style.FILL);

        paintCirculo.setStyle(Paint.Style.FILL);
        paintCirculo.setAntiAlias(true);

        paintNumero.setColor(Color.BLACK);
        paintNumero.setTextSize(36f);
        paintNumero.setTypeface(Typeface.DEFAULT_BOLD);
        paintNumero.setTextAlign(Paint.Align.CENTER);
        paintNumero.setAntiAlias(true);

        paintNombre.setColor(Color.WHITE);
        paintNombre.setTextSize(28f);
        paintNombre.setTypeface(Typeface.DEFAULT_BOLD);
        paintNombre.setTextAlign(Paint.Align.CENTER);
        paintNombre.setAntiAlias(true);

        paintSombra.setColor(0x55000000);
        paintSombra.setStyle(Paint.Style.FILL);
        paintSombra.setAntiAlias(true);
    }

    // ── API pública ─────────────────────────────────────────────

    public void setLineup(String formacion, List<PlayerSlot> jugadores) {
        this.formacion = formacion != null ? formacion : "4-3-3";
        this.jugadores = jugadores != null ? jugadores : new ArrayList<>();
        invalidate();
    }

    // ── Dibujo ──────────────────────────────────────────────────

    // Store player positions for touch detection
    private final java.util.List<float[]> posicionesJugadores = new java.util.ArrayList<>();
    private final java.util.List<PlayerSlot> slotsEnOrden = new java.util.ArrayList<>();
    private float radioUltimo = 0f;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        posicionesJugadores.clear();
        slotsEnOrden.clear();

        float w = getWidth();
        float h = getHeight();

        dibujarCesped(canvas, w, h);
        dibujarLineas(canvas, w, h);
        dibujarJugadores(canvas, w, h);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_UP && clickListener != null) {
            float tx = event.getX();
            float ty = event.getY();
            for (int i = 0; i < posicionesJugadores.size(); i++) {
                float[] pos = posicionesJugadores.get(i);
                double dist = Math.sqrt(Math.pow(tx - pos[0], 2) + Math.pow(ty - pos[1], 2));
                if (dist <= radioUltimo * 1.3f) {
                    clickListener.onPlayerClick(slotsEnOrden.get(i));
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Dibuja el césped con franjas alternadas para efecto visual.
     */
    private void dibujarCesped(Canvas canvas, float w, float h) {
        int nFranjas = 8;
        float anchoFranja = h / nFranjas;
        for (int i = 0; i < nFranjas; i++) {
            paintCesped.setColor(i % 2 == 0 ? COLOR_CAMPO : COLOR_CAMPO2);
            canvas.drawRect(0, i * anchoFranja, w, (i + 1) * anchoFranja, paintCesped);
        }
    }

    /**
     * Dibuja las líneas del campo: bordes, línea central, círculo central,
     * áreas grande y pequeña de ambos porteros, y punto de penalti.
     */
    private void dibujarLineas(Canvas canvas, float w, float h) {
        float pad = w * 0.04f; // padding lateral

        // Borde del campo
        canvas.drawRect(pad, pad, w - pad, h - pad, paintLineas);

        // Línea central
        canvas.drawLine(pad, h / 2f, w - pad, h / 2f, paintLineas);

        // Círculo central
        float radio = w * 0.12f;
        canvas.drawCircle(w / 2f, h / 2f, radio, paintLineas);
        // Punto central
        paintCesped.setColor(Color.WHITE);
        canvas.drawCircle(w / 2f, h / 2f, 6f, paintCesped);

        // Área grande superior (portero arriba = visitante o local según orientación)
        float areaAncho = w * 0.55f;
        float areaAlto  = h * 0.13f;
        float areaX     = (w - areaAncho) / 2f;
        canvas.drawRect(areaX, pad, areaX + areaAncho, pad + areaAlto, paintLineas);

        // Área pequeña superior
        float areaPAncho = w * 0.28f;
        float areaPAlto  = h * 0.06f;
        float areaPX     = (w - areaPAncho) / 2f;
        canvas.drawRect(areaPX, pad, areaPX + areaPAncho, pad + areaPAlto, paintLineas);

        // Área grande inferior
        canvas.drawRect(areaX, h - pad - areaAlto, areaX + areaAncho, h - pad, paintLineas);

        // Área pequeña inferior
        canvas.drawRect(areaPX, h - pad - areaPAlto, areaPX + areaPAncho, h - pad, paintLineas);

        // Puntos de penalti
        paintCesped.setColor(Color.WHITE);
        canvas.drawCircle(w / 2f, pad + h * 0.18f, 5f, paintCesped);
        canvas.drawCircle(w / 2f, h - pad - h * 0.18f, 5f, paintCesped);

        // Semicírculo del área superior
        RectF arcRect = new RectF(w/2f - radio, pad + areaAlto - radio,
                w/2f + radio, pad + areaAlto + radio);
        canvas.drawArc(arcRect, 0, 180, false, paintLineas);

        // Semicírculo del área inferior
        RectF arcRect2 = new RectF(w/2f - radio, h - pad - areaAlto - radio,
                w/2f + radio, h - pad - areaAlto + radio);
        canvas.drawArc(arcRect2, 180, 180, false, paintLineas);
    }

    /**
     * Posiciona a los 11 jugadores en el campo según la formación.
     * Los jugadores se dibujan de portero (arriba) a delanteros (abajo),
     * con el campo visto desde arriba.
     */
    private void dibujarJugadores(Canvas canvas, float w, float h) {
        if (jugadores.isEmpty()) return;

        // Parsear formación: "4-3-3" → [4, 3, 3] o "4-2-3-1" → [4, 2, 3, 1]
        int[] lineas = parsearFormacion(formacion);

        // Calcular posiciones Y: portero + N líneas de campo
        int totalLineas = 1 + lineas.length;
        float[] yLineas = calcularYLineas(totalLineas, h);

        float radioJugador = w * 0.07f;

        // Repartir jugadores en líneas según la formación
        // Orden: portero (1) + cada línea de la formación
        List<List<PlayerSlot>> gruposPorLinea = new ArrayList<>();

        // Separar por posición manteniendo orden original
        List<PlayerSlot> porteros   = filtrar("G");
        List<PlayerSlot> defensas   = filtrar("D");
        List<PlayerSlot> medios     = filtrar("M");
        List<PlayerSlot> delanteros = filtrar("F");

        gruposPorLinea.add(porteros.subList(0, Math.min(1, porteros.size())));

        // Repartir el resto según los números de la formación
        // lineas[0]=defensas, lineas[1..N-2]=medios, lineas[N-1]=delanteros
        gruposPorLinea.add(defensas.subList(0, Math.min(lineas[0], defensas.size())));

        if (lineas.length == 3) {
            // 3 líneas: def - med - del
            gruposPorLinea.add(medios.subList(0, Math.min(lineas[1], medios.size())));
            gruposPorLinea.add(delanteros.subList(0, Math.min(lineas[2], delanteros.size())));
        } else if (lineas.length == 4) {
            // 4 líneas: def - med1 - med2 - del
            int med1 = lineas[1];
            int med2 = lineas[2];
            gruposPorLinea.add(medios.subList(0, Math.min(med1, medios.size())));
            gruposPorLinea.add(medios.subList(Math.min(med1, medios.size()),
                    Math.min(med1 + med2, medios.size())));
            gruposPorLinea.add(delanteros.subList(0, Math.min(lineas[3], delanteros.size())));
        } else {
            // Fallback: todo lo que quede
            gruposPorLinea.add(medios.subList(0, Math.min(lineas.length > 1 ? lineas[1] : 3, medios.size())));
            gruposPorLinea.add(delanteros.subList(0, Math.min(lineas.length > 2 ? lineas[2] : 3, delanteros.size())));
        }

        // Colores por línea
        int[] colores = {COLOR_PORTERO, COLOR_DEFENSA, COLOR_MEDIO, COLOR_MEDIO, COLOR_DELANTERO};

        // Dibujar cada línea
        for (int i = 0; i < gruposPorLinea.size() && i < yLineas.length; i++) {
            int color = i < colores.length ? colores[i] : COLOR_MEDIO;
            // Último grupo siempre es delanteros
            if (i == gruposPorLinea.size() - 1) color = COLOR_DELANTERO;
            dibujarLinea(canvas, gruposPorLinea.get(i), w, yLineas[i], radioJugador, color);
        }
    }

    private void dibujarLinea(Canvas canvas, List<PlayerSlot> jugadoresLinea,
                              float w, float y, float radio, int color) {
        if (jugadoresLinea.isEmpty()) return;

        int n = jugadoresLinea.size();
        float espaciado = w / (n + 1);

        for (int i = 0; i < n; i++) {
            float x = espaciado * (i + 1);
            PlayerSlot j = jugadoresLinea.get(i);

            // Guardar posición para detección de toque
            posicionesJugadores.add(new float[]{x, y});
            slotsEnOrden.add(jugadoresLinea.get(i));
            radioUltimo = radio;

            // Sombra
            paintSombra.setColor(0x55000000);
            canvas.drawCircle(x + 3, y + 3, radio, paintSombra);

            // Círculo del jugador
            paintCirculo.setColor(color);
            canvas.drawCircle(x, y, radio, paintCirculo);

            // Borde blanco
            paintLineas.setStrokeWidth(2.5f);
            paintLineas.setColor(Color.WHITE);
            canvas.drawCircle(x, y, radio, paintLineas);

            // Icono persona (dibujado con Canvas)
            dibujarIconoPersona(canvas, x, y, radio, Color.BLACK);

            // Nombre debajo
            String apellido = apellido(j.nombre);
            paintNombre.setColor(Color.WHITE);
            paintNombre.setTextSize(radio * 0.62f);

            // Fondo semitransparente para el nombre
            float textW = paintNombre.measureText(apellido);
            paintSombra.setColor(0x88000000);
            canvas.drawRoundRect(
                    new RectF(x - textW/2 - 4, y + radio + 2, x + textW/2 + 4, y + radio + radio * 0.7f + 2),
                    4, 4, paintSombra);

            canvas.drawText(apellido, x, y + radio + radio * 0.75f, paintNombre);
        }
    }

    /**
     * Dibuja un icono de persona simple (cabeza + torso) dentro del círculo.
     */
    private void dibujarIconoPersona(Canvas canvas, float cx, float cy, float radio, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);

        float cabezaR  = radio * 0.30f;
        float cabezaY  = cy - radio * 0.20f;
        float torsoTop = cabezaY + cabezaR + radio * 0.05f;
        float torsoBot = cy + radio * 0.60f;
        float torsoW   = radio * 0.50f;

        // Cabeza
        canvas.drawCircle(cx, cabezaY, cabezaR, p);

        // Torso (semicírculo para dar forma de cuerpo)
        RectF torso = new RectF(cx - torsoW, torsoTop, cx + torsoW, torsoBot + torsoW);
        canvas.drawArc(torso, 180, 180, true, p);
    }

    // ── Helpers ─────────────────────────────────────────────────

    private int[] parsearFormacion(String f) {
        try {
            String[] partes = f.split("-");
            int[] result = new int[partes.length];
            for (int i = 0; i < partes.length; i++) result[i] = Integer.parseInt(partes[i].trim());
            return result;
        } catch (Exception e) {
            return new int[]{4, 3, 3};
        }
    }

    private float[] calcularYLineas(int totalLineas, float h) {
        float[] ys = new float[totalLineas];
        float margenTop    = h * 0.09f;
        float margenBottom = h * 0.12f; // más margen abajo para los nombres
        float espacioUtil  = h - margenTop - margenBottom;
        float paso = totalLineas > 1 ? espacioUtil / (totalLineas - 1) : 0;
        for (int i = 0; i < totalLineas; i++) {
            ys[i] = margenTop + i * paso;
        }
        return ys;
    }

    private List<PlayerSlot> filtrar(String pos) {
        List<PlayerSlot> result = new ArrayList<>();
        for (PlayerSlot j : jugadores) {
            if (pos.equals(j.posicion)) result.add(j);
        }
        return result;
    }

    /** Filtra medios pero solo los primeros 'n1' o solo los últimos 'n2' */
    private List<PlayerSlot> filtrar2(String pos, int n1, int n2) {
        List<PlayerSlot> todos = filtrar(pos);
        if (todos.size() <= n1) return todos;
        return todos.subList(n1, Math.min(todos.size(), n1 + n2));
    }

    private String apellido(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "?";
        // Si tiene formato "Inicial. Apellido" tomamos todo
        String[] partes = nombre.trim().split(" ");
        return partes.length > 1 ? partes[partes.length - 1] : nombre;
    }
}