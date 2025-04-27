package com.example.lab3_20190057;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab3_20190057.network.TriviaResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TriviaActivity extends AppCompatActivity {

    private String categoria;
    private int cantidad;
    private String dificultad;
    private int tiempoPorPregunta;
    private int tiempoTotal;

    private TextView tvTimer;
    private TextView tvNumeroPregunta;
    private TextView tvPregunta;
    private RadioGroup rgRespuesta;
    private RadioButton rbTrue;
    private RadioButton rbFalse;
    private MaterialButton btnSiguiente;

    private TriviaViewModel viewModel;
    private List<TriviaResponse.TriviaQuestion> preguntas;
    private int preguntaActual = 0;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        // Inicializar componentes UI
        tvTimer = findViewById(R.id.tv_timer);
        tvNumeroPregunta = findViewById(R.id.tvNumeroPregunta);
        tvPregunta = findViewById(R.id.tvPregunta);
        rgRespuesta = findViewById(R.id.rgRespuestaUsuario);
        rbTrue = findViewById(R.id.rbTrue);
        rbFalse = findViewById(R.id.rbFalse);
        btnSiguiente = findViewById(R.id.btn_siguiente);

        // Obtener los datos del Intent
        if (getIntent() != null) {
            categoria = getIntent().getStringExtra("CATEGORIA");
            cantidad = getIntent().getIntExtra("CANTIDAD", 5);
            dificultad = getIntent().getStringExtra("DIFICULTAD");

            TextView categoriaPreguntas = findViewById(R.id.tvCategoriaPreguntas);
            categoriaPreguntas.setText(categoria + " Knowledge");

            // Calcular tiempo según dificultad
            if (dificultad.equalsIgnoreCase("fácil")) {
                tiempoPorPregunta = 5;
            } else if (dificultad.equalsIgnoreCase("medio")) {
                tiempoPorPregunta = 7;
            } else if (dificultad.equalsIgnoreCase("difícil")) {
                tiempoPorPregunta = 10;
            } else {
                // Valor por defecto por si hay algún problema
                tiempoPorPregunta = 7;
                Toast.makeText(this, "Usando tiempo predeterminado para dificultad: " + dificultad, Toast.LENGTH_SHORT).show();
            }

            tiempoTotal = cantidad * tiempoPorPregunta;
        }

        // Inicializar ViewModel y observar cambios
        viewModel = new ViewModelProvider(this).get(TriviaViewModel.class);

        viewModel.getQuestions().observe(this, questions -> {
            if (questions != null && !questions.isEmpty()) {
                preguntas = questions;
                mostrarPregunta(0);
                iniciarContador(tiempoTotal);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Configurar RadioGroup para habilitar el botón cuando se selecciona una respuesta
        rgRespuesta.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                btnSiguiente.setEnabled(true);
            }
        });

        // Configurar botón Siguiente
        btnSiguiente.setOnClickListener(v -> procesarRespuesta());

        // Obtener preguntas desde la API
        viewModel.obtenerPreguntas(categoria, cantidad, dificultad);
    }

    private void mostrarPregunta(int index) {
        if (preguntas != null && index < preguntas.size()) {
            TriviaResponse.TriviaQuestion pregunta = preguntas.get(index);
            tvNumeroPregunta.setText("Pregunta " + (index + 1) + "/" + preguntas.size());
            tvPregunta.setText(Html.fromHtml(pregunta.getQuestion(), Html.FROM_HTML_MODE_LEGACY));

            // Resetear selección de radio button
            rgRespuesta.clearCheck();
            btnSiguiente.setEnabled(false);
        }
    }

    private void iniciarContador(int segundos) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(segundos * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int mins = (int) (millisUntilFinished / 1000) / 60;
                int secs = (int) (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                finalizarTrivia();
            }
        }.start();
    }

    private void procesarRespuesta() {
        if (preguntas == null || preguntaActual >= preguntas.size()) {
            return;
        }

        TriviaResponse.TriviaQuestion preguntaActualObj = preguntas.get(preguntaActual);
        String respuestaCorrecta = preguntaActualObj.getCorrect_answer();
        String respuestaUsuario = rbTrue.isChecked() ? "True" : "False";

        if (respuestaUsuario.equalsIgnoreCase(respuestaCorrecta)) {
            viewModel.registrarRespuestaCorrecta();
        } else {
            viewModel.registrarRespuestaIncorrecta();
        }

        preguntaActual++;

        if (preguntaActual < preguntas.size()) {
            mostrarPregunta(preguntaActual);
        } else {
            finalizarTrivia();
        }
    }

    private void finalizarTrivia() {
        if (timer != null) {
            timer.cancel();
        }

        Intent intent = new Intent(TriviaActivity.this, ResultActivity.class);
        intent.putExtra("CORRECTAS", viewModel.getRespuestasCorrectas());
        intent.putExtra("INCORRECTAS", viewModel.getRespuestasIncorrectas());
        intent.putExtra("SIN_RESPONDER", viewModel.getRespuestasSinResponder());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}