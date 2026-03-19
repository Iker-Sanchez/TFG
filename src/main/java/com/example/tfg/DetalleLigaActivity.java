package com.example.tfg;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleLigaActivity extends AppCompatActivity {

    private TableLayout tableStandings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liga_detalle); // El XML que creamos antes

        tableStandings = findViewById(R.id.tableStandings);

        // Ejemplo de datos para probar la interfaz
        agregarFila(1, "Barcelona", 16, 13, 1, 2, 40);
        agregarFila(2, "Real Madrid", 16, 11, 3, 2, 36);
    }

    private void agregarFila(int rank, String nombre, int pj, int v, int e, int d, int pts) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 25, 0, 25);

        TextView txtNombre = new TextView(this);
        txtNombre.setText(rank + "  " + nombre);
        txtNombre.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

        row.addView(txtNombre);
        row.addView(crearCelda(String.valueOf(pj), false));
        row.addView(crearCelda(String.valueOf(v), false));
        row.addView(crearCelda(String.valueOf(e), false));
        row.addView(crearCelda(String.valueOf(d), false));
        row.addView(crearCelda(String.valueOf(pts), true));

        tableStandings.addView(row);
    }

    private TextView crearCelda(String texto, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setGravity(Gravity.CENTER);
        if (negrita) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }
}