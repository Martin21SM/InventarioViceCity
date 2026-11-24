package com.devst.inventariovicecity.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devst.inventariovicecity.R;
import com.devst.inventariovicecity.model.Articulo;
import com.devst.inventariovicecity.viewmodel.InventarioViewModel;

public class MainActivity extends AppCompatActivity {

    private InventarioViewModel viewModel;
    private ArticuloAdapter adapter;

    private EditText etCodigo;
    private EditText etNombre;
    private EditText etCategoria;
    private EditText etCantidad;
    private EditText etPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Deja espacio para la barra de estado y para el teclado, así nada queda tapado
        ScrollView scroll = findViewById(R.id.scrollPrincipal);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            vista.setPadding(barras.left, barras.top, barras.right,
                    Math.max(barras.bottom, teclado.bottom));
            return insets;
        });

        etCodigo = findViewById(R.id.etCodigo);
        etNombre = findViewById(R.id.etNombre);
        etCategoria = findViewById(R.id.etCategoria);
        etCantidad = findViewById(R.id.etCantidad);
        etPrecio = findViewById(R.id.etPrecio);

        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnBuscar = findViewById(R.id.btnBuscar);
        Button btnActualizar = findViewById(R.id.btnActualizar);
        Button btnEliminar = findViewById(R.id.btnEliminar);

        RecyclerView lista = findViewById(R.id.rvInventario);
        adapter = new ArticuloAdapter();
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(InventarioViewModel.class);

        viewModel.getInventario().observe(this, articulos -> adapter.establecerDatos(articulos));
        viewModel.getArticuloEncontrado().observe(this, this::mostrarArticulo);
        viewModel.getToastMessage().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                viewModel.limpiarToast();
            }
        });

        btnGuardar.setOnClickListener(v -> viewModel.guardarArticulo(
                texto(etCodigo), texto(etNombre), texto(etCategoria),
                texto(etCantidad), texto(etPrecio)));

        btnActualizar.setOnClickListener(v -> viewModel.actualizarArticulo(
                texto(etCodigo), texto(etNombre), texto(etCategoria),
                texto(etCantidad), texto(etPrecio)));

        btnBuscar.setOnClickListener(v -> viewModel.buscarArticulo(texto(etCodigo)));

        btnEliminar.setOnClickListener(v -> viewModel.eliminarArticulo(texto(etCodigo)));
    }

    // Llena el formulario con el artículo encontrado. Si llega null, limpia los campos.
    private void mostrarArticulo(Articulo articulo) {
        if (articulo == null) {
            etCodigo.setText("");
            etNombre.setText("");
            etCategoria.setText("");
            etCantidad.setText("");
            etPrecio.setText("");
            return;
        }
        etCodigo.setText(articulo.getCodigo());
        etNombre.setText(articulo.getNombre());
        etCategoria.setText(articulo.getCategoria());
        etCantidad.setText(String.valueOf(articulo.getCantidad()));
        etPrecio.setText(String.valueOf(articulo.getPrecio()));
    }

    private static String texto(EditText campo) {
        return campo.getText().toString().trim();
    }
}
