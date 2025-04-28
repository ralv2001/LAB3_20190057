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
        tvPregunta = findViewById(R.id.tvPregunta);  // Asegúrate de que esta línea existe
        rgRespuesta = findViewById(R.id.rgRespuestaUsuario);
        rbTrue = findViewById(R.id.rbTrue);
        rbFalse = findViewById(R.id.rbFalse);
        btnSiguiente = findViewById(R.id.btn_siguiente);

        // Obtener los datos del Intent
        if (getIntent() != null) {
            categoria = getIntent().getStringExtra("CATEGORIA");
            cantidad = getIntent().getIntExtra("CANTIDAD", 5);
            dificultad = getIntent().getStringExtra("DIFICULTAD");

            // Agregar estos logs de depuración
            System.out.println("DEBUG: TriviaActivity onCreate - Categoría: " + categoria);
            System.out.println("DEBUG: TriviaActivity onCreate - Cantidad: " + cantidad);
            System.out.println("DEBUG: TriviaActivity onCreate - Dificultad: " + dificultad);

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
                // Volver a la actividad principal después de un error
                new android.os.Handler().postDelayed(() -> {
                    finish();
                }, 3000); // Esperar 3 segundos para que el usuario vea el mensaje
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

// Reemplaza el método mostrarPregunta en TriviaActivity.java con esta versión mejorada

    private void mostrarPregunta(int index) {
        System.out.println("DEBUG: mostrarPregunta - index: " + index);
        System.out.println("DEBUG: mostrarPregunta - preguntas: " + (preguntas != null ? preguntas.size() : "null"));

        if (preguntas != null && index < preguntas.size()) {
            TriviaResponse.TriviaQuestion pregunta = preguntas.get(index);
            tvNumeroPregunta.setText("Pregunta " + (index + 1) + "/" + preguntas.size());

            // Debug para ver la pregunta recibida
            System.out.println("DEBUG: Pregunta recibida: " + pregunta.getQuestion());

            // Decodificar HTML entities
            String preguntaTexto = Html.fromHtml(pregunta.getQuestion(), Html.FROM_HTML_MODE_LEGACY).toString();
            tvPregunta.setText(preguntaTexto);

            // Resetear selección de radio button
            rgRespuesta.clearCheck();
            btnSiguiente.setEnabled(false);
        } else {
            System.out.println("DEBUG: Error en mostrarPregunta - preguntas null o index fuera de rango");
            if (preguntas == null) {
                System.out.println("DEBUG: preguntas es NULL");
            } else {
                System.out.println("DEBUG: index=" + index + ", preguntas.size=" + preguntas.size());
            }
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

    // Añade estos métodos al final de la clase TriviaActivity.java (justo antes del último corchete)

    @Override
    protected void onPause() {
        super.onPause();
        // No cancelamos el timer aquí para que siga corriendo
        // cuando se rota la pantalla o se cambia entre aplicaciones
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Aquí podríamos actualizar la UI si fuera necesario
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // No necesitamos hacer nada especial aquí, ya que queremos mantener el estado actual
        System.out.println("DEBUG: onConfigurationChanged - Orientación cambiada");

        // Actualizar la UI si es necesario
        if (preguntas != null && preguntaActual < preguntas.size()) {
            // Asegurarse de que la pregunta actual se siga mostrando correctamente
            tvNumeroPregunta.setText("Pregunta " + (preguntaActual + 1) + "/" + preguntas.size());
        }
    }
}