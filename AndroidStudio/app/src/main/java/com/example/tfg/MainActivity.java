package com.example.tfg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
<<<<<<< HEAD

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int ID_LALIGA = 140;
    private static final int ID_CHAMPIONS = 2;

=======
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
=======
        // Vincular botones con el XML
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
        Button btnLaLiga = findViewById(R.id.btnLaLiga);
        Button btnPremier = findViewById(R.id.btnPremier);
        Button btnChampions = findViewById(R.id.btnChampions);

<<<<<<< HEAD
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
=======
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
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
            }
        });
    }

    private void abrirPantallaLiga(int idLiga) {
        Intent intent = new Intent(MainActivity.this, DetalleLigaActivity.class);
        intent.putExtra("ID_LIGA_SELECCIONADA", idLiga);
        startActivity(intent);
    }
<<<<<<< HEAD

    private void abrirPantallaPremier() {
        Intent intent = new Intent(MainActivity.this, DetallePremierActivity.class);
        startActivity(intent);
    }
=======
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
}