package com.example.tfg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int ID_LALIGA = 140;
    private static final int ID_CHAMPIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLaLiga = findViewById(R.id.btnLaLiga);
        Button btnPremier = findViewById(R.id.btnPremier);
        Button btnChampions = findViewById(R.id.btnChampions);

        btnLaLiga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirPantallaLiga(ID_LALIGA);
            }
        });

        btnPremier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirPantallaPremier();
            }
        });

        btnChampions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirPantallaLiga(ID_CHAMPIONS);
            }
        });
    }

    private void abrirPantallaLiga(int idLiga) {
        Intent intent = new Intent(MainActivity.this, DetalleLigaActivity.class);
        intent.putExtra("ID_LIGA_SELECCIONADA", idLiga);
        startActivity(intent);
    }

    private void abrirPantallaPremier() {
        Intent intent = new Intent(MainActivity.this, DetallePremierActivity.class);
        startActivity(intent);
    }
}