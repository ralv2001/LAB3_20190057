package com.example.lab3_20190057;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lab3_20190057.network.RetrofitClient;
import com.example.lab3_20190057.network.TriviaResponse;
import com.example.lab3_20190057.network.TriviaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TriviaViewModel extends ViewModel {
    private MutableLiveData<List<TriviaResponse.TriviaQuestion>> triviaQuestions = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();

    // Mapeo de categorías a sus IDs
    private static final Map<String, Integer> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("Cultura General", 9);
        CATEGORY_MAP.put("Libros", 10);
        CATEGORY_MAP.put("Películas", 11);
        CATEGORY_MAP.put("Música", 12);
        CATEGORY_MAP.put("Computación", 18);
        CATEGORY_MAP.put("Matemática", 19);
        CATEGORY_MAP.put("Deportes", 21);
        CATEGORY_MAP.put("Historia", 23);
    }

    // Mapeo de dificultades en español a inglés
    private static final Map<String, String> DIFFICULTY_MAP = new HashMap<>();
    static {
        DIFFICULTY_MAP.put("fácil", "easy");
        DIFFICULTY_MAP.put("medio", "medium");
        DIFFICULTY_MAP.put("difícil", "hard");
    }

    // Variables para almacenar contadores de respuestas
    private int respuestasCorrectas = 0;
    private int respuestasIncorrectas = 0;
    private int respuestasSinResponder = 0;

    public void obtenerPreguntas(String categoria, int cantidad, String dificultad) {
        isLoading.setValue(true);

        // Verificación para debugging
        System.out.println("Categoría seleccionada: " + categoria);
        System.out.println("Dificultad seleccionada: " + dificultad);

        Integer categoryId = CATEGORY_MAP.get(categoria);
        if (categoryId == null) {
            // Si no encuentra la categoría exacta, intentamos buscar con trim()
            for (Map.Entry<String, Integer> entry : CATEGORY_MAP.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(categoria.trim())) {
                    categoryId = entry.getValue();
                    break;
                }
            }

            if (categoryId == null) {
                error.setValue("Categoría no válida: " + categoria);
                isLoading.setValue(false);
                return;
            }
        }

        String difficultyStr = DIFFICULTY_MAP.get(dificultad.toLowerCase());
        if (difficultyStr == null) {
            error.setValue("Dificultad no válida: " + dificultad);
            isLoading.setValue(false);
            return;
        }

        TriviaService service = RetrofitClient.getClient().create(TriviaService.class);
        Call<TriviaResponse> call = service.getTrivia(cantidad, categoryId, difficultyStr, "boolean");

        // Log para debugging
        System.out.println("URL de la llamada: " + call.request().url());

        call.enqueue(new Callback<TriviaResponse>() {
            @Override
            public void onResponse(Call<TriviaResponse> call, Response<TriviaResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    triviaQuestions.setValue(response.body().getResults());

                    // Inicializar contadores y configurar preguntas sin responder
                    respuestasCorrectas = 0;
                    respuestasIncorrectas = 0;
                    respuestasSinResponder = response.body().getResults().size();
                } else {
                    error.setValue("Error al obtener preguntas: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TriviaResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    public LiveData<List<TriviaResponse.TriviaQuestion>> getQuestions() {
        return triviaQuestions;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Métodos para gestionar los contadores de respuestas
    public void registrarRespuestaCorrecta() {
        respuestasCorrectas++;
        respuestasSinResponder--;
    }

    public void registrarRespuestaIncorrecta() {
        respuestasIncorrectas++;
        respuestasSinResponder--;
    }

    public int getRespuestasCorrectas() {
        return respuestasCorrectas;
    }

    public int getRespuestasIncorrectas() {
        return respuestasIncorrectas;
    }

    public int getRespuestasSinResponder() {
        return respuestasSinResponder;
    }
}