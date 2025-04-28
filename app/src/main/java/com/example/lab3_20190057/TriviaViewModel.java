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
    public static final Map<String, Integer> CATEGORY_MAP = new HashMap<>();
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
        System.out.println("DEBUG: ViewModel - Categoría seleccionada: " + categoria);
        System.out.println("DEBUG: ViewModel - Cantidad seleccionada: " + cantidad);
        System.out.println("DEBUG: ViewModel - Dificultad seleccionada: " + dificultad);

        // Primero, verifica disponibilidad de preguntas para asegurarte de que hay suficientes
        Integer categoryId = null;
        for (Map.Entry<String, Integer> entry : CATEGORY_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(categoria)) {
                categoryId = entry.getValue();
                break;
            }
        }

        if (categoryId == null) {
            System.out.println("DEBUG: ViewModel - Categoría no válida: " + categoria);
            error.setValue("Categoría no válida: " + categoria);
            isLoading.setValue(false);
            return;
        }

        String difficultyStr = DIFFICULTY_MAP.get(dificultad.toLowerCase());
        if (difficultyStr == null) {
            System.out.println("DEBUG: ViewModel - Dificultad no válida: " + dificultad);
            error.setValue("Dificultad no válida: " + dificultad);
            isLoading.setValue(false);
            return;
        }

        // Hacer la solicitud final
        TriviaService service = RetrofitClient.getClient().create(TriviaService.class);
        Call<TriviaResponse> call = service.getTrivia(cantidad, categoryId, difficultyStr, "boolean");

        // Log para debugging
        System.out.println("DEBUG: ViewModel - URL de la llamada: " + call.request().url());

        call.enqueue(new Callback<TriviaResponse>() {
            @Override
            public void onResponse(Call<TriviaResponse> call, Response<TriviaResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    TriviaResponse triviaResponse = response.body();
                    System.out.println("DEBUG: ViewModel - Respuesta exitosa, código: " + triviaResponse.getResponse_code());

                    // Imprimir la URL completa de nuevo para debugging
                    System.out.println("DEBUG: ViewModel - URL de la respuesta: " + call.request().url());

                    // Manejar específicamente el código de respuesta 1 (No Results)
                    if (triviaResponse.getResponse_code() != 0) {
                        System.out.println("DEBUG: ViewModel - Respuesta no exitosa (código " + triviaResponse.getResponse_code() + ")");
                        // Corregir los mapeos de códigos de respuesta
                        String errorMessage;
                        switch (triviaResponse.getResponse_code()) {
                            case 1:
                                errorMessage = "No hay suficientes preguntas para esta categoría/dificultad. Intenta con otra combinación o menor cantidad.";
                                break;
                            case 2:
                                errorMessage = "Parámetro de consulta inválido. Verifica la categoría o dificultad.";
                                break;
                            default:
                                errorMessage = "Error desconocido al obtener preguntas (código " + triviaResponse.getResponse_code() + ").";
                        }
                        error.setValue(errorMessage);
                        return;
                    }


                    if (triviaResponse.getResults() != null && !triviaResponse.getResults().isEmpty()) {
                        System.out.println("DEBUG: ViewModel - Número de preguntas recibidas: " + triviaResponse.getResults().size());
                        triviaQuestions.setValue(triviaResponse.getResults());

                        // Inicializar contadores y configurar preguntas sin responder
                        respuestasCorrectas = 0;
                        respuestasIncorrectas = 0;
                        respuestasSinResponder = triviaResponse.getResults().size();
                    } else {
                        System.out.println("DEBUG: ViewModel - No se recibieron preguntas en la respuesta");
                        error.setValue("No se recibieron preguntas. Intenta con otra categoría o cantidad.");
                    }
                } else {
                    System.out.println("DEBUG: ViewModel - Error en la respuesta: " + response.message());
                    System.out.println("DEBUG: ViewModel - Código de respuesta HTTP: " + response.code());

                    // Manejo específico para error 429 (Too Many Requests)
                    if (response.code() == 429) {
                        error.setValue("Has alcanzado el límite de solicitudes a la API. Por favor, espera unos minutos antes de intentar nuevamente.");
                    } else {
                        error.setValue("Error al obtener preguntas: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<TriviaResponse> call, Throwable t) {
                isLoading.setValue(false);
                System.out.println("DEBUG: ViewModel - Error de conexión: " + t.getMessage());
                t.printStackTrace();
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