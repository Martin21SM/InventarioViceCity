package com.devst.inventariovicecity.model;

/**
 * Un artículo del inventario (vehículo, ropa, accesorio, etc.).
 * El precio va en dólares enteros, como "$185000".
 */
public class Articulo {
    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final int cantidad;
    private final long precio;

    public Articulo(String codigo, String nombre, String categoria, int cantidad, long precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getCantidad() {
        return cantidad;
    }

    public long getPrecio() {
        return precio;
    }

    // Valor de todas las piezas de este artículo
    public long getValorTotal() {
        return precio * cantidad;
    }
}
