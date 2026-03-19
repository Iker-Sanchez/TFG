package com.example.tfg;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Vincular botones con el XML
        Button btnLaLiga = findViewById(R.id.btnLaLiga);
        Button btnPremier = findViewById(R.id.btnPremier);
        Button btnChampions = findViewById(R.id.btnChampions);
        // Evento clic para La Liga
        btnLaLiga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaLiga(140);
            }
        });
        // Evento clic para Premier League
        btnPremier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaLiga(39);
            }
        });
        // Evento clic para Champions League
        btnChampions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaLiga(2);
            }
        });
    }
    private void abrirPantallaLiga(int idLiga) {
        Intent intent = new Intent(MainActivity.this, DetalleLigaActivity.class);
        intent.putExtra("ID_LIGA_SELECCIONADA", idLiga);
        startActivity(intent);
    }
}