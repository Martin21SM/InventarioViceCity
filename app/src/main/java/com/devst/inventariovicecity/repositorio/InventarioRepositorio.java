package com.devst.inventariovicecity.repositorio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.devst.inventariovicecity.model.Articulo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Único lugar que toca los datos. Guarda en SQLite (dentro del celular)
 * y además envía una copia a Cloud Firestore (colección "articulos").
 */
public class InventarioRepositorio {

    private static final String TAG = "InventarioRepo";
    private static final String COLECCION = "articulos";

    // Un solo hilo en segundo plano: la base de datos nunca se usa en el hilo principal.
    private static final ExecutorService HILO_BD = Executors.newSingleThreadExecutor();

    private final InventarioDbHelper dbHelper;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Lista completa del inventario, se actualiza después de cada cambio
    private final MutableLiveData<List<Articulo>> inventario = new MutableLiveData<>();

    public InventarioRepositorio(Context context) {
        dbHelper = new InventarioDbHelper(context.getApplicationContext());
    }

    public LiveData<List<Articulo>> getInventario() {
        return inventario;
    }

    // READ ALL: vuelve a cargar la lista completa y la sincroniza con Firestore
    public void refrescarInventario() {
        HILO_BD.execute(() -> {
            try {
                sincronizarTodo(publicarInventario(dbHelper.getReadableDatabase()));
            } catch (Exception e) {
                Log.w(TAG, "Error al cargar el inventario", e);
            }
        });
    }

    // CREATE: solo crea artículos nuevos (si el código ya existe, avisa)
    public void crear(Articulo articulo, MutableLiveData<String> toastMessage) {
        HILO_BD.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (existe(db, articulo.getCodigo())) {
                    toastMessage.postValue("Ya existe un artículo con ese código. Usa Actualizar.");
                    return;
                }
                long fila = db.insert(InventarioDbHelper.TABLA, null, valoresDe(articulo));
                if (fila == -1) {
                    toastMessage.postValue("Error al guardar el artículo");
                } else {
                    Log.d(TAG, "Artículo creado: " + articulo.getCodigo());
                    subirAFirestore(articulo);
                    toastMessage.postValue("Artículo guardado");
                }
                publicarInventario(db);
            } catch (Exception e) {
                Log.w(TAG, "Error al guardar artículo", e);
                toastMessage.postValue("Error al guardar: " + e.getMessage());
            }
        });
    }

    // UPDATE: solo modifica artículos que ya existen
    public void actualizar(Articulo articulo, MutableLiveData<String> toastMessage) {
        HILO_BD.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int filas = db.update(InventarioDbHelper.TABLA, valoresDe(articulo),
                        InventarioDbHelper.COL_CODIGO + " = ?", new String[]{articulo.getCodigo()});
                if (filas > 0) {
                    subirAFirestore(articulo);
                    toastMessage.postValue("Artículo actualizado");
                } else {
                    toastMessage.postValue("No existe un artículo con ese código");
                }
                publicarInventario(db);
            } catch (Exception e) {
                Log.w(TAG, "Error al actualizar artículo", e);
                toastMessage.postValue("Error al actualizar: " + e.getMessage());
            }
        });
    }

    // READ ONE
    public void buscar(String codigo, MutableLiveData<Articulo> resultado, MutableLiveData<String> toastMessage) {
        HILO_BD.execute(() -> {
            try (Cursor cursor = dbHelper.getReadableDatabase().query(
                    InventarioDbHelper.TABLA, null,
                    InventarioDbHelper.COL_CODIGO + " = ?", new String[]{codigo},
                    null, null, null)) {

                if (cursor.moveToFirst()) {
                    resultado.postValue(leer(cursor));
                    toastMessage.postValue("Artículo encontrado");
                } else {
                    resultado.postValue(null);
                    toastMessage.postValue("Artículo no encontrado");
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al buscar artículo", e);
                toastMessage.postValue("Error al buscar: " + e.getMessage());
            }
        });
    }

    // DELETE
    public void eliminar(String codigo, MutableLiveData<String> toastMessage) {
        HILO_BD.execute(() -> {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int filas = db.delete(InventarioDbHelper.TABLA,
                        InventarioDbHelper.COL_CODIGO + " = ?", new String[]{codigo});
                if (filas > 0) {
                    borrarDeFirestore(codigo);
                    toastMessage.postValue("Artículo eliminado");
                } else {
                    toastMessage.postValue("No existe un artículo con ese código");
                }
                publicarInventario(db);
            } catch (Exception e) {
                Log.w(TAG, "Error al eliminar artículo", e);
                toastMessage.postValue("Error al eliminar: " + e.getMessage());
            }
        });
    }

    // --- Firestore ---

    private void subirAFirestore(Articulo a) {
        firestore.collection(COLECCION).document(a.getCodigo()).set(mapaDe(a))
                .addOnSuccessListener(v -> Log.d(TAG, "Firestore OK: " + a.getCodigo()))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error al guardar", e));
    }

    private void borrarDeFirestore(String codigo) {
        firestore.collection(COLECCION).document(codigo).delete()
                .addOnSuccessListener(v -> Log.d(TAG, "Firestore borrado: " + codigo))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error al borrar", e));
    }

    // Sube todo el inventario local (incluye los artículos de ejemplo)
    private void sincronizarTodo(List<Articulo> lista) {
        if (lista.isEmpty()) return;
        WriteBatch batch = firestore.batch();
        for (Articulo a : lista) {
            batch.set(firestore.collection(COLECCION).document(a.getCodigo()), mapaDe(a));
        }
        batch.commit()
                .addOnSuccessListener(v -> Log.d(TAG, "Firestore sincronizado: " + lista.size()))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error al sincronizar", e));
    }

    private static Map<String, Object> mapaDe(Articulo a) {
        Map<String, Object> m = new HashMap<>();
        m.put("codigo", a.getCodigo());
        m.put("nombre", a.getNombre());
        m.put("categoria", a.getCategoria());
        m.put("cantidad", a.getCantidad());
        m.put("precio", a.getPrecio());
        return m;
    }

    // --- Ayudas internas (siempre se llaman desde el hilo de la base de datos) ---

    private static boolean existe(SQLiteDatabase db, String codigo) {
        try (Cursor cursor = db.query(InventarioDbHelper.TABLA,
                new String[]{InventarioDbHelper.COL_CODIGO},
                InventarioDbHelper.COL_CODIGO + " = ?", new String[]{codigo},
                null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    private List<Articulo> publicarInventario(SQLiteDatabase db) {
        List<Articulo> lista = new ArrayList<>();
        try (Cursor cursor = db.query(InventarioDbHelper.TABLA, null,
                null, null, null, null,
                InventarioDbHelper.COL_NOMBRE + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                lista.add(leer(cursor));
            }
        }
        inventario.postValue(lista);
        return lista;
    }

    private static ContentValues valoresDe(Articulo articulo) {
        ContentValues valores = new ContentValues();
        valores.put(InventarioDbHelper.COL_CODIGO, articulo.getCodigo());
        valores.put(InventarioDbHelper.COL_NOMBRE, articulo.getNombre());
        valores.put(InventarioDbHelper.COL_CATEGORIA, articulo.getCategoria());
        valores.put(InventarioDbHelper.COL_CANTIDAD, articulo.getCantidad());
        valores.put(InventarioDbHelper.COL_PRECIO, articulo.getPrecio());
        return valores;
    }

    private static Articulo leer(Cursor cursor) {
        return new Articulo(
                cursor.getString(cursor.getColumnIndexOrThrow(InventarioDbHelper.COL_CODIGO)),
                cursor.getString(cursor.getColumnIndexOrThrow(InventarioDbHelper.COL_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(InventarioDbHelper.COL_CATEGORIA)),
                cursor.getInt(cursor.getColumnIndexOrThrow(InventarioDbHelper.COL_CANTIDAD)),
                cursor.getLong(cursor.getColumnIndexOrThrow(InventarioDbHelper.COL_PRECIO)));
    }
}