package com.devst.inventariovicecity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.devst.inventariovicecity.model.Articulo;
import com.devst.inventariovicecity.repositorio.InventarioRepositorio;

import java.util.List;

public class InventarioViewModel extends AndroidViewModel {

    private final InventarioRepositorio repository;

    // LiveData para notificar a la Vista (Activity)
    private final MutableLiveData<Articulo> articuloEncontrado = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public InventarioViewModel(@NonNull Application application) {
        super(application);
        repository = new InventarioRepositorio(application);
        repository.refrescarInventario();
    }

    // --- Getters para que la Vista "observe" ---

    public LiveData<List<Articulo>> getInventario() {
        return repository.getInventario();
    }

    public LiveData<Articulo> getArticuloEncontrado() {
        return articuloEncontrado;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    // La Vista llama esto después de mostrar un mensaje, para que no se repita al girar la pantalla.
    public void limpiarToast() {
        toastMessage.setValue(null);
    }

    // --- Métodos que la Vista llamará ---

    public void guardarArticulo(String codigo, String nombre, String categoria,
                                String cantidadTxt, String precioTxt) {
        Articulo articulo = validar(codigo, nombre, categoria, cantidadTxt, precioTxt);
        if (articulo != null) {
            repository.crear(articulo, toastMessage);
        }
    }

    public void actualizarArticulo(String codigo, String nombre, String categoria,
                                   String cantidadTxt, String precioTxt) {
        Articulo articulo = validar(codigo, nombre, categoria, cantidadTxt, precioTxt);
        if (articulo != null) {
            repository.actualizar(articulo, toastMessage);
        }
    }

    public void buscarArticulo(String codigo) {
        if (codigo.isEmpty()) {
            toastMessage.setValue("Escribe un código para buscar");
            return;
        }
        repository.buscar(codigo, articuloEncontrado, toastMessage);
    }

    public void eliminarArticulo(String codigo) {
        if (codigo.isEmpty()) {
            toastMessage.setValue("Escribe un código para eliminar");
            return;
        }
        // Limpiamos los campos de la UI mientras se elimina
        articuloEncontrado.setValue(null);
        repository.eliminar(codigo, toastMessage);
    }

    // Revisa los campos y convierte cantidad y precio a números. Devuelve null si hay un error.
    private Articulo validar(String codigo, String nombre, String categoria,
                             String cantidadTxt, String precioTxt) {
        if (codigo.isEmpty() || nombre.isEmpty() || categoria.isEmpty()
                || cantidadTxt.isEmpty() || precioTxt.isEmpty()) {
            toastMessage.setValue("Completa código, nombre, categoría, cantidad y precio");
            return null;
        }

        int cantidad;
        long precio;
        try {
            cantidad = Integer.parseInt(cantidadTxt);
            precio = Long.parseLong(precioTxt);
        } catch (NumberFormatException e) {
            toastMessage.setValue("Cantidad y precio deben ser números enteros");
            return null;
        }

        if (cantidad < 0 || precio < 0) {
            toastMessage.setValue("Cantidad y precio no pueden ser negativos");
            return null;
        }

        return new Articulo(codigo, nombre, categoria, cantidad, precio);
    }
}
