package com.example.lab3_20190057;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ResultActivity extends AppCompatActivity {

    private TextView tvRespuestasCorrectas;
    private TextView tvRespuestasIncorrectas;
    private TextView tvRespuestasSinResponder;
    private MaterialButton btnVolverAJugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvRespuestasCorrectas = findViewById(R.id.tvRespuestasCorrectas);
        tvRespuestasIncorrectas = findViewById(R.id.tvRespuestasIncorrectas);
        tvRespuestasSinResponder = findViewById(R.id.tvRespuestasSinResponder);
        btnVolverAJugar = findViewById(R.id.btn_volver_jugar);

        // Obtener datos del Intent
        int correctas = getIntent().getIntExtra("CORRECTAS", 0);
        int incorrectas = getIntent().getIntExtra("INCORRECTAS", 0);
        int sinResponder = getIntent().getIntExtra("SIN_RESPONDER", 0);

        // Mostrar resultados
        tvRespuestasCorrectas.setText(String.valueOf(correctas));
        tvRespuestasIncorrectas.setText(String.valueOf(incorrectas));
        tvRespuestasSinResponder.setText(String.valueOf(sinResponder));

        // Configurar botón para volver a jugar
        btnVolverAJugar.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}