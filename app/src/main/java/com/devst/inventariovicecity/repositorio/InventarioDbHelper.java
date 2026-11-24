package com.devst.inventariovicecity.repositorio;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Crea la base de datos local (SQLite) donde se guarda el inventario en el celular.
 */
class InventarioDbHelper extends SQLiteOpenHelper {

    static final String NOMBRE_BD = "inventario.db";
    static final int VERSION_BD = 1;

    static final String TABLA = "articulos";
    static final String COL_CODIGO = "codigo";
    static final String COL_NOMBRE = "nombre";
    static final String COL_CATEGORIA = "categoria";
    static final String COL_CANTIDAD = "cantidad";
    static final String COL_PRECIO = "precio";

    InventarioDbHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + COL_CODIGO + " TEXT PRIMARY KEY NOT NULL, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_CATEGORIA + " TEXT NOT NULL, "
                + COL_CANTIDAD + " INTEGER NOT NULL, "
                + COL_PRECIO + " INTEGER NOT NULL)");

        // Artículos de ejemplo para que la lista no empiece vacía.
        insertarEjemplo(db, "ART-001", "Auto deportivo", "Vehículo", 1, 185000);
        insertarEjemplo(db, "ART-002", "Moto de carreras", "Vehículo", 2, 42000);
        insertarEjemplo(db, "ART-003", "Chaqueta de cuero", "Ropa", 3, 1500);
        insertarEjemplo(db, "ART-004", "Gorra neón", "Ropa", 5, 250);
        insertarEjemplo(db, "ART-005", "Teléfono inteligente", "Accesorio", 4, 800);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionVieja, int versionNueva) {
        // Por ahora solo existe la versión 1. Al cambiar la tabla, aquí se migran los datos.
    }

    private static void insertarEjemplo(SQLiteDatabase db, String codigo, String nombre,
                                        String categoria, int cantidad, long precio) {
        ContentValues valores = new ContentValues();
        valores.put(COL_CODIGO, codigo);
        valores.put(COL_NOMBRE, nombre);
        valores.put(COL_CATEGORIA, categoria);
        valores.put(COL_CANTIDAD, cantidad);
        valores.put(COL_PRECIO, precio);
        db.insert(TABLA, null, valores);
    }
}