package com.example.lab3_20190057;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.ProgressDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.lab3_20190057.network.CategoryCountResponse;
import com.example.lab3_20190057.network.RetrofitClient;
import com.example.lab3_20190057.network.TriviaResponse;
import com.example.lab3_20190057.network.TriviaService;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab3_20190057.R;
import com.example.lab3_20190057.network.CategoryCountResponse;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategoria;
    private EditText editCantidad;
    private Spinner spinnerDificultad;
    private Button btnComprobar;
    private Button btnComenzar;
    private boolean conexionComprobada = false;

    // Resto de la clase...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar los componentes de la UI
        Spinner spinnerCategoria = findViewById(R.id.spinner_categoria);
        editCantidad = findViewById(R.id.edit_cantidad);
        Spinner spinnerDificultad = findViewById(R.id.spinner_dificultad);
        btnComprobar = findViewById(R.id.btn_comprobar);
        btnComenzar = findViewById(R.id.btn_comenzar);

        // Configurar el adapter para el spinner de categorías
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(
                this, R.array.categorias_con_placeholder, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Configurar el adapter para el spinner de dificultad
        ArrayAdapter<CharSequence> adapterDificultad = ArrayAdapter.createFromResource(
                this, R.array.dificultades_con_placeholder, android.R.layout.simple_spinner_item);
        adapterDificultad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapterDificultad);

        // Configurar los listeners para los botones
        btnComprobar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarConexion();
            }
        });

        btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comenzarJuego();
            }
        });

        // Guardar referencias a los spinners para usarlos en el método comenzarJuego()
        this.spinnerCategoria = spinnerCategoria;
        this.spinnerDificultad = spinnerDificultad;
    }

    private void comprobarConexion() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                Toast.makeText(this, "Conexión a Internet establecida", Toast.LENGTH_SHORT).show();
                conexionComprobada = true;
                btnComenzar.setEnabled(true);
            } else {
                Toast.makeText(this, "Error: No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                conexionComprobada = false;
                btnComenzar.setEnabled(false);
            }
        } else {
            Toast.makeText(this, "Error: No se puede verificar la conexión", Toast.LENGTH_SHORT).show();
            conexionComprobada = false;
            btnComenzar.setEnabled(false);
        }
    }

    private void comenzarJuego() {
        // Primero, verificar la conexión a internet nuevamente
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean tieneInternet = false;

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            tieneInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        if (!tieneInternet) {
            Toast.makeText(this, "Error: Se perdió la conexión a Internet", Toast.LENGTH_SHORT).show();
            conexionComprobada = false;
            btnComenzar.setEnabled(false);
            return;
        }

        // Para la categoría
        String categoria = "";
        if (spinnerCategoria.getSelectedItemPosition() > 0) {
            categoria = spinnerCategoria.getSelectedItem().toString();
        } else {
            Toast.makeText(this, "Por favor seleccione una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        String cantidadStr = editCantidad.getText().toString().trim();

        // Para la dificultad
        String dificultad = "";
        if (spinnerDificultad.getSelectedItemPosition() > 0) {
            dificultad = spinnerDificultad.getSelectedItem().toString();
        } else {
            Toast.makeText(this, "Por favor seleccione una dificultad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug logs
        System.out.println("Categoría seleccionada: " + categoria);
        System.out.println("Cantidad seleccionada: " + cantidadStr);
        System.out.println("Dificultad seleccionada: " + dificultad);

        if (categoria.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la categoría sea válida
        String[] categorias = getResources().getStringArray(R.array.categorias);
        boolean categoriaValida = false;
        for (String c : categorias) {
            if (c.equalsIgnoreCase(categoria)) {
                categoriaValida = true;
                categoria = c; // Usar el formato exacto del array
                break;
            }
        }

        if (!categoriaValida) {
            Toast.makeText(this, "Categoría no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que todos los campos estén llenos
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese la cantidad de preguntas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dificultad.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una dificultad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la dificultad sea válida
        String[] dificultades = getResources().getStringArray(R.array.dificultades);
        boolean dificultadValida = false;
        for (String d : dificultades) {
            if (d.equalsIgnoreCase(dificultad)) {
                dificultadValida = true;
                dificultad = d; // Usar el formato exacto del array
                break;
            }
        }

        if (!dificultadValida) {
            Toast.makeText(this, "Dificultad no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que la cantidad sea un número positivo
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser un número positivo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cantidad > 50) {
                Toast.makeText(this, "La API solo permite un máximo de 50 preguntas", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que se haya comprobado la conexión
        if (!conexionComprobada) {
            Toast.makeText(this, "Por favor, compruebe la conexión a Internet primero", Toast.LENGTH_SHORT).show();
            return;
        }

        verificarDisponibilidadPreguntas(categoria, cantidad, dificultad);
    }

    private void verificarDisponibilidadPreguntas(String categoria, int cantidad, String dificultad) {
        // Mostrar un diálogo de carga
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Iniciando trivia...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Obtener ID de categoría
        Integer categoryId = null;
        for (Map.Entry<String, Integer> entry : TriviaViewModel.CATEGORY_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(categoria)) {
                categoryId = entry.getValue();
                break;
            }
        }

        if (categoryId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Categoría no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar juego directamente, ya que el ViewModel manejará la lógica de fallback
        progressDialog.dismiss();
        iniciarJuego(categoria, cantidad, dificultad);
    }

    private void iniciarJuego(String categoria, int cantidad, String dificultad) {
        // Crear un Intent para pasar a la siguiente actividad
        Intent intent = new Intent(MainActivity.this, TriviaActivity.class);

        // Pasar los datos seleccionados como extras
        intent.putExtra("CATEGORIA", categoria);
        intent.putExtra("CANTIDAD", cantidad);
        intent.putExtra("DIFICULTAD", dificultad);

        // Iniciar la actividad
        startActivity(intent);
    }
}